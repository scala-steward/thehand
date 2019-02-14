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
import scala.util.{Failure, Success}


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

  def writeTasks(tasks: Seq[Task]) = {
    def upsert(task: Task, taskId: Option[Long]) =  {
      if (taskId.isEmpty) (Query.tasks += task) else Query.tasks.insertOrUpdate(task.copy(id = taskId.head))
    }
    def taskQuery(task: Task) = {
      for {
        taskId <- Query.tasks.filter(_.taskId === task.taskId).map(_.id).result.headOption
        u <- upsert(task, taskId).asTry
      } yield u
    }
    exec(DBIO.sequence(tasks.map(taskQuery(_))).transactionally)
  }

  def writeAuthors(authors: Seq[Author]) = {
    def upsert(author: Author, authorIds: Option[Long]) =  {
      if (authorIds.isEmpty) (Query.authors += author) else Query.authors.insertOrUpdate(author.copy(author.author, authorIds.head))
    }
    def authorQuery(author: Author) = {
      for {
        authorId <- Query.authors.filter(_.author === author.author).map(_.id).result.headOption
        u <- upsert(author, authorId).asTry
      } yield u
    }
    exec(DBIO.sequence(authors.map(authorQuery(_))).transactionally)
  }

  def writeCommits(commits: Seq[(CommitEntry, String)]) = {
    def upsert(commit: CommitEntry, commitId: Option[Long], authorId: Option[Long]) = {
      if (commitId.isEmpty)
        (Query.commits += commit.copy(authorId = authorId.head))
      else
        Query.commits.insertOrUpdate(commit.copy(authorId = authorId.head, id = commitId.head))
    }
    def commitQuery(entry: (CommitEntry, String)) = {
      val (commit, authorName) = entry
      for {
        authorId <- Query.authors.filter(_.author === authorName).map(_.id).result.headOption
        commitId <- Query.commits.filter(_.revision === commit.revision).map(_.id).result.headOption
        u <- upsert(commit, commitId, authorId).asTry
      } yield u
    }
    exec(DBIO.sequence(commits.map(commitQuery)).transactionally)
  }

  def writeFiles(files: Seq[EntryFile]) = {
    def upsert(file: EntryFile, id: Option[Long]) = {
      if (id.isEmpty) (Query.files += file) else Query.files.insertOrUpdate(file.copy(id = id.head))
    }
    def fileQuery(file: EntryFile) = {
      for {
        fileId <- Query.files.filter(_.path === file.path).map(_.id).result.headOption
        u <- upsert(file, fileId).asTry
      } yield u
    }
    exec(DBIO.sequence(files.map(fileQuery(_))).transactionally)
  }

  def writeCommitsFiles(entries: Seq[(Seq[CommitEntryWriter], Long)]) = {
    def fileQuery(fileEntries: (Seq[CommitEntryWriter], Long)) = {
      val (entryFiles, revisionNumber) = fileEntries

      def tryFindFileId(path: Option[String]): DBIO[Option[Long]] = path match {
        case Some(p) => if (p == null) DBIOAction.successful(Some(-1)) else Query.files.filter(_.path === p).map(_.id).result.headOption
        case None => DBIOAction.successful(Some(-1))
      }

      def upsert(c: CommitEntryWriter, id: Option[Long], commitId: Long, copyFileId: Option[Long], fileId: Option[Long]) = {
        if (id.isEmpty)
          (Query.commitsFiles += c.commit.copy(copyPath = copyFileId, pathId = fileId.get, revisionId = commitId))
        else
          Query.commitsFiles.insertOrUpdate(c.commit.copy(copyPath = copyFileId, pathId = fileId.get, revisionId = commitId, id = id.head))
      }

      def insertFilePath(c: CommitEntryWriter, commitId: Long) = for {
        fileId <- tryFindFileId(Some(c.path))
        copyFileId <- tryFindFileId(Some(c.pathCopy))
        id <- Query.commitsFiles.filter(_.revisionId === commitId).filter(_.pathId === fileId).map(_.id).result.headOption
        u <- upsert(c, id, commitId, copyFileId, fileId).asTry
      } yield u

      def insert(files: Seq[CommitEntryWriter]) = for {
        commitId <- Query.commits.filter(_.revision === revisionNumber).map(_.id).result.headOption
        _ <- DBIO.sequence(files.map(insertFilePath(_, commitId.getOrElse(-1)))).asTry
      } yield commitId

      insert(entryFiles)
    }

    exec(DBIO.sequence(entries.map(fileQuery)).transactionally)
  }

  def writeCommitsTasks(entries: Seq[CommitTasks]) = {
    def upsert(ct: CommitTasks, id: Option[Long], commitId: Option[Long]) = {
      if (id.isEmpty)
        (Query.commitTasks += ct.copy(commitId = commitId.head))
      else
        Query.commitTasks.insertOrUpdate(ct.copy(commitId = commitId.head, id = commitId.head))
    }
    def swapRevisionByTableId(revision: Long) = {
      Query.commits.filter(_.revision === revision).map(_.id).result.headOption
    }
    def tryInsert(commitTask: CommitTasks) = for {
      commitId <- swapRevisionByTableId(commitTask.commitId)
      id <- Query.commitTasks.filter(_.id === commitTask.id).map(_.id).result.headOption
      u <- upsert(commitTask, id ,commitId).asTry
    } yield u
    exec(DBIO.sequence(entries.map(tryInsert)).transactionally)
  }

  // test

  def countCommitTasks: Future[Int] = exec(Query.commitTasks.size.result)

  def countTasks: Future[Int] = exec(Query.tasks.size.result)

  def countFiles: Future[Int] = exec(Query.files.size.result)

  def countAuthors: Future[Int] = exec(Query.authors.size.result)

  def countCommits: Future[Int] = exec(Query.commits.size.result)

  def countCommitsFiles: Future[Int] = exec(Query.commitsFiles.size.result)
}