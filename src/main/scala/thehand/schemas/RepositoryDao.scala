/*
 * Copyright (c) 2018, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 *
 *
 */

package thehand.schemas

import slick.dbio.DBIOAction
import slick.jdbc.JdbcProfile
import telemetrics.HandLogger

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class RepositoryDao(databaseProfile: JdbcProfile, configPath: String, suffix: String = "") {
  val databaseLayer = new DatabaseLayer(databaseProfile)

  import databaseLayer._
  import databaseProfile.api._

  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)

  private lazy val db: databaseProfile.backend.DatabaseDef = Database.forConfig(configPath)
  createSchemas()

  object Query {
    lazy val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => CommitTasksTable(tag, suffix))
    lazy val tasks = TableQuery[TaskTable]((tag: Tag) => TaskTable(tag, suffix))
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => EntryFilesTable(tag, suffix))
    lazy val authors = TableQuery[AuthorsTable]((tag: Tag) => AuthorsTable(tag, suffix))
    lazy val commits = TableQuery[CommitTable]((tag: Tag) => CommitTable(tag, suffix))
    lazy val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => CommitEntryFileTable(tag, suffix))
  }

  def createSchemas(): Unit = {
    exec(
        Query.tasks.schema.create.asTry andThen
        Query.authors.schema.create.asTry andThen
        Query.commits.schema.create.asTry andThen
        Query.files.schema.create.asTry andThen
        Query.commitsFiles.schema.create.asTry andThen
        Query.commitTasks.schema.create.asTry) onComplete {
      case Success(_) => HandLogger.debug("correct create tables")
      case Failure(e) =>
        HandLogger.error("error in create tables " + e.getMessage)
    }
  }

  private def exec[T](program: DBIO[T]): Future[T] =
    Await.ready(db.run(program), 10 minutes)

  def close(): Unit = {
    db.close()
  }

  def actionLatestRevision(): Future[Option[Long]] =
    exec(Query.commits.map(_.revision).max.result)

  def latestId: Long = Await.result(actionLatestRevision(), 10 seconds) match {
    case Some(s) => s
    case None => 1L
  }

  def writeTasks(tasks : Seq[Task]): Future[Seq[Try[Int]]] =
    exec(DBIO.sequence(tasks.map(task => (Query.tasks += task).asTry)).transactionally)

  def writeAuthors(authors: Seq[Author]): Future[Seq[Try[Int]]] =
    exec(DBIO.sequence(authors.map(author => (Query.authors += author).asTry)).transactionally)

  def writeCommits(commits: Seq[(CommitEntry, String)]): Future[Seq[Try[Int]]] = {
    def commitQuery(entry: (CommitEntry, String)) = {
      val (commit, authorName) = entry
      for {
        authorId <- Query.authors.filter(_.author === authorName).map(_.id).result.headOption
        rowsAffected <- (Query.commits += commit.copy(authorId = authorId.getOrElse(-1))).asTry
      } yield rowsAffected
    }
    exec(DBIO.sequence(commits.map(commitQuery)))
  }

  def writeFiles(files: Seq[EntryFile]): Future[Seq[Try[Int]]] =
    exec(DBIO.sequence(files.map(file => (Query.files += file).asTry)).transactionally)

  def writeCommitsFiles(entries: Seq[(Seq[CommitEntryWriter], Long)]) = {
    def fileQuery(fileEntries: (Seq[CommitEntryWriter], Long)) = {
      val (entryFiles, revisionNumber) = fileEntries
      println(revisionNumber)

      def tryFindFileId(path: Option[String]): DBIO[Option[Long]] = path match {
        case Some(p) => if (p == null) DBIOAction.successful(Some(-1)) else Query.files.filter(_.path === p).map(_.id).result.headOption
        case None => DBIOAction.successful(Some(-1))
      }

      def insertFilePath(c: CommitEntryWriter, commitId: Long) = for {
        fileId <- tryFindFileId(Some(c.path))
        copyFileId <- tryFindFileId(Some(c.pathCopy))
        rowsAffected <- (Query.commitsFiles += c.commit.copy(c.commit.typeModification, copyPath = copyFileId, c.commit.copyRevision, pathId = fileId.get, revisionId = commitId)).asTry
      } yield rowsAffected

      def insert(files: Seq[CommitEntryWriter]) = for {
        commitId <- Query.commits.filter(_.revision === revisionNumber).map(_.id).result.headOption
        _ <- DBIO.sequence(files.map(insertFilePath(_, commitId.getOrElse(-1)))).asTry
      } yield commitId

      insert(entryFiles)
    }

    exec(DBIO.sequence(entries.map(fileQuery)).transactionally)
  }

  def writeCommitsTasks(entries: Seq[CommitTasks]): Future[Seq[Option[Long]]] = {
    def swapRevisionByTableId(revision: Long) = Query.commits.filter(_.revision === revision).map(_.id).result.headOption
    def tryInsert(commitTask: CommitTasks) = for {
      commitId <- swapRevisionByTableId(commitTask.commitId)
      _ <- (Query.commitTasks += commitTask.copy(commitId = commitId.getOrElse(-1))).asTry
    } yield commitId
    exec(DBIO.sequence(entries.map(tryInsert)).transactionally)
  }

  // report

  def filesBugsCounter: Future[Seq[(String, Int)]] = {
    val bugs = for {
      co <- Query.commits
      cf <- Query.commitsFiles if cf.revisionId === co.id
      fi <- Query.files if fi.id === cf.pathId
      ct <- Query.commitTasks if ct.commitId === co.id
      tk <- Query.tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L
    } yield fi
    val countBugs = bugs
      .groupBy(_.path)
      .map {
      case (path, group) =>
        (path, group.length)
    }
    exec(countBugs.result.transactionally)
  }

  // test

  def countCommitTasks: Future[Int] = exec(Query.commitTasks.size.result)

  def countTasks: Future[Int] = exec(Query.tasks.size.result)

  def countFiles: Future[Int] = exec(Query.files.size.result)

  def countAuthors: Future[Int] = exec(Query.authors.size.result)

  def countCommits: Future[Int] = exec(Query.commits.size.result)

  def countCommitsFiles: Future[Int] = exec(Query.commitsFiles.size.result)
}