/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package thehand.schemas

import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class ReportsDao(databaseProfile: JdbcProfile, configPath: String, suffix: String = "") {
  val databaseLayer = new DatabaseLayer(databaseProfile)

  import databaseLayer._
  import databaseProfile.api._

  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)

  private lazy val db: databaseProfile.backend.DatabaseDef = Database.forConfig(configPath)

  private def exec[T](program: DBIO[T]): Future[T] =
    Await.ready(db.run(program), 2 minutes)

  def close(): Unit = {
    db.close()
  }

  object Query {
    lazy val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => CommitTasksTable(tag, suffix))
    lazy val tasks = TableQuery[TaskTable]((tag: Tag) => TaskTable(tag, suffix))
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => EntryFilesTable(tag, suffix))
    lazy val authors = TableQuery[AuthorsTable]((tag: Tag) => AuthorsTable(tag, suffix))
    lazy val commits = TableQuery[CommitTable]((tag: Tag) => CommitTable(tag, suffix))
    lazy val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => CommitEntryFileTable(tag, suffix))
  }

  def filesBugsCounter: Future[Seq[(String, Int)]] = {
    val bugs = for {
      co <- Query.commits
      cf <- Query.commitsFiles if cf.revisionId === co.id
      fi <- Query.files if fi.id === cf.pathId
      ct <- Query.commitTasks if ct.commitId === co.id
      tk <- Query.tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
    } yield fi
    val countBugs = bugs
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    exec(countBugs.result.transactionally)
  }

  def fileAuthorCommitsCounter(author: String): Future[Seq[(String, Int)]] = {
    val commits = for {
      ai <- Query.authors if ai.author === author
      co <- Query.commits if co.authorId === ai.id
      cf <- Query.commitsFiles if cf.revisionId === co.id
      fi <- Query.files if fi.id === cf.pathId
    } yield fi
    val countCommits = commits
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    exec(countCommits.result.transactionally)
  }

  def fileAuthorCommitsBugsCounter(author: String): Future[Seq[(String, Int)]] = {
    val commits = for {
      ai <- Query.authors if ai.author === author
      co <- Query.commits if co.authorId === ai.id
      cf <- Query.commitsFiles if cf.revisionId === co.id
      fi <- Query.files if fi.id === cf.pathId
      ct <- Query.commitTasks if ct.commitId === co.id
      tk <- Query.tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
    } yield fi
    val countCommits = commits
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    exec(countCommits.result.transactionally)
  }

  def filterMovedFiles(revisionId: Long): Future[Seq[CommitEntryFile]] = {
    val files = for {
      cf <- Query.commitsFiles if cf.revisionId === revisionId && cf.copyPathId >= 1L
    } yield cf
    exec(files.result.transactionally)
  }

  def authorsNames = {
    exec(Query.authors.map(_.author).result)
  }
}
