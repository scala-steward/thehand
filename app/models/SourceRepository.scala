/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

/**
  * A repository for taks.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class SourceRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  object PortableJodaSupport extends com.github.tototoshi.slick.GenericJodaSupport(profile)
  import PortableJodaSupport._

  private class AuthorsTable(tag: Tag, suffix: String) extends Table[Author](tag, suffix +"authors") {
    def author: Rep[String] = column[String]("author", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[Author] = (author, id) <> ((Author.apply _).tupled, Author.unapply)
  }

  def listAuthors_s(suffix : String): Future[Seq[Author]] = db.run {
    lazy val autors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    autors.result
  }




  private class CommitTable(tag: Tag, suffix: String) extends Table[CommitEntry](tag, suffix + "commits") {
    def message: Rep[Option[String]] = column[Option[String]]("message")
    def date: Rep[Option[DateTime]] = column[Option[DateTime]]("date")
    def revision: Rep[Long] = column[Long]("revision", O.Unique)
    def authorId: Rep[Long] = column[Long]("author")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[CommitEntry] = (message, date, revision, authorId, id) <> ((CommitEntry.apply _).tupled, CommitEntry.unapply)
    //def author = foreignKey("author_fk", authorId, TableQuery[AuthorsTable]((tag:Tag) => AuthorsTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.SetNull)
  }

  def listCommit_s(suffix : String): Future[Seq[CommitEntry]] = db.run {
    lazy val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.result
  }





  private class CommitTasksTable(tag: Tag, suffix: String) extends Table[CommitTasks](tag, suffix + "committasks") {
    def taskId: Rep[Long] = column[Long]("task_id")
    def commitId: Rep[Long] = column[Long]("commit_id")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[CommitTasks] = (taskId, commitId, id) <> ((CommitTasks.apply _).tupled, CommitTasks.unapply)
    //def commit = foreignKey("commit_fk", commitId, TableQuery[CommitTable]((tag:Tag) => CommitTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  def listCommitTasks_s(suffix : String): Future[Seq[CommitTasks]] = db.run {
    lazy val commits = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
    commits.result
  }




  private class CommitEntryFileTable(tag: Tag, suffix: String) extends Table[CommitEntryFile](tag, suffix + "commitfiles") {
    def typeModification: Rep[Option[Char]] = column[Option[Char]]("typeModification")
    def copyPathId: Rep[Option[Long]] = column[Option[Long]]("copyPath_id")
    def copyRevisionId: Rep[Option[Long]] = column[Option[Long]]("copyRevision")
    def pathId: Rep[Long] = column[Long]("path_id")
    def revisionId: Rep[Long] = column[Long]("revision")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[CommitEntryFile] = (typeModification, copyPathId, copyRevisionId, pathId, revisionId, id) <> ((CommitEntryFile.apply _).tupled, CommitEntryFile.unapply)
    //def revision = foreignKey("revision_fk", revisionId, TableQuery[CommitTable]((tag:Tag) => CommitTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
    //def path = foreignKey("path_fk", pathId, TableQuery[EntryFilesTable]((tag:Tag) => EntryFilesTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  def listCommitEntryFile_s(suffix : String): Future[Seq[CommitEntryFile]] = db.run {
    lazy val commits = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    commits.result
  }




  private class EntryFilesTable(tag: Tag, suffix: String) extends Table[EntryFile](tag, suffix + "files") {
    def path: Rep[String] = column[String]("path", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * : ProvenShape[EntryFile] = (path, id) <> ((EntryFile.apply _).tupled, EntryFile.unapply)
  }

  def listEntryFiles_s(suffix : String): Future[Seq[EntryFile]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    files.result
  }


//  private val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, "eb_"))
//
//
//  def list_s(suffix : String): Future[Seq[Task]] = db.run {
//    lazy val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
//    tasks.result
//  }
//
//  def list(): Future[Seq[Task]] = db.run {
//    tasks.result
//  }
}

