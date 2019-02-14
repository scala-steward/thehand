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

import org.joda.time.DateTime

trait SourceTables {
  this: Profile => import profile.api._
  object PortableJodaSupport extends com.github.tototoshi.slick.GenericJodaSupport(profile)
  import PortableJodaSupport._

  object AuthorsTable {
    def apply(tag: Tag, suffix: String): AuthorsTable = new AuthorsTable(tag, suffix)
  }

  final class AuthorsTable(tag: Tag, suffix: String) extends Table[Author](tag, suffix +"authors") {
    def author = column[String]("author", O.Unique)
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (author, id).mapTo[Author]
  }

  object CommitTable {
    def apply(tag: Tag, suffix: String): CommitTable = new CommitTable(tag, suffix)
  }

  final class CommitTable(tag: Tag, suffix: String) extends Table[CommitEntry](tag, suffix + "commits") {
    def message = column[Option[String]]("message")
    def date = column[Option[DateTime]]("date")
    def revision = column[Long]("revision", O.Unique)
    def authorId = column[Long]("author")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (message, date, revision, authorId, id).mapTo[CommitEntry]
    def author = foreignKey("author_fk", authorId, TableQuery[AuthorsTable]((tag:Tag) => AuthorsTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.SetNull)
  }

  object CommitTasksTable {
    def apply(tag: Tag, suffix: String): CommitTasksTable = new CommitTasksTable(tag, suffix)
  }
  
  final class CommitTasksTable(tag: Tag, suffix: String) extends Table[CommitTasks](tag, suffix + "committasks") {
    def taskId = column[Long]("task_id")
    def commitId = column[Long]("commit_id")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (taskId, commitId, id).mapTo[CommitTasks]
    def commit = foreignKey("commit_fk", commitId, TableQuery[CommitTable]((tag:Tag) => CommitTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  object CommitEntryFileTable {
    def apply(tag: Tag, suffix: String): CommitEntryFileTable = new CommitEntryFileTable(tag, suffix)
  }

  final class CommitEntryFileTable(tag: Tag, suffix: String) extends Table[CommitEntryFile](tag, suffix + "commitfiles") {
    def typeModification = column[Option[Char]]("typeModification")
    def copyPathId = column[Option[Long]]("copyPath_id")
    def copyRevisionId = column[Option[Long]]("copyRevision")
    def pathId = column[Long]("path_id")
    def revisionId = column[Long]("revision")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (typeModification, copyPathId, copyRevisionId, pathId, revisionId, id).mapTo[CommitEntryFile]
    def revision = foreignKey("revision_fk", revisionId, TableQuery[CommitTable]((tag:Tag) => CommitTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
    def path = foreignKey("path_fk", pathId, TableQuery[EntryFilesTable]((tag:Tag) => EntryFilesTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
  }

  object EntryFilesTable {
    def apply(tag: Tag, suffix: String): EntryFilesTable = new EntryFilesTable(tag, suffix)
  }

  final class EntryFilesTable(tag: Tag, suffix: String) extends Table[EntryFile](tag, suffix + "files") {
    def path = column[String]("path", O.Unique)
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (path, id).mapTo[EntryFile]
  }
}
