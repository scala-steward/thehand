package dao

import javax.inject.Inject

import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait EntryFileComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class EntryFilesTable(tag: Tag, suffix: Suffix) extends Table[EntryFile](tag, suffix.suffix + "files") {
    def path: Rep[String] = column[String]("path", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (path, id) <> ((EntryFile.apply _).tupled, EntryFile.unapply)
  }
}

class EntryFileDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends EntryFileComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(fs: Seq[EntryFile], suffix: Suffix): Future[Seq[Int]] = db.run {
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    def updateInsert(file: EntryFile, id: Option[Long]) = {
      if (id.isEmpty) files += file else files.insertOrUpdate(file.copy(id = id.head))
    }

    def fileQuery(file: EntryFile) = {
      for {
        fileId <- files.filter(_.path === file.path).map(_.id).result.headOption
        u <- updateInsert(file, fileId) //.asTry
      } yield u
    }

    DBIO.sequence(fs.map(fileQuery)).transactionally
  }

  def list(suffix: Suffix): Future[Seq[EntryFile]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    files.result
  }

  def info(suffix: Suffix, id: Long): Future[Seq[EntryFile]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    files.filter(_.id === id).result
  }
}