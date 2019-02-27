package dao

import javax.inject.{ Inject, Singleton }

import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait EntryFileComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class EntryFilesTable(tag: Tag, suffix: Suffix) extends Table[EntryFile](tag, suffix + "files") {
    def path: Rep[String] = column[String]("path", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (path, id) <> ((EntryFile.apply _).tupled, EntryFile.unapply)
  }
}

@Singleton()
class EntryFileDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, suffix: Suffix)
  extends EntryFileComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))

  def insert(fs: Seq[EntryFile]): Future[Seq[Int]] = db.run {
    def upsert(file: EntryFile, id: Option[Long]) = {
      if (id.isEmpty) files += file else files.insertOrUpdate(file.copy(id = id.head))
    }

    def fileQuery(file: EntryFile) = {
      for {
        fileId <- files.filter(_.path === file.path).map(_.id).result.headOption
        u <- upsert(file, fileId)//.asTry
      } yield u
    }

    DBIO.sequence(fs.map(fileQuery)).transactionally
  }

  def listEntryFiles_s(suffix : Suffix): Future[Seq[EntryFile]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    files.result
  }

  def countFiles: Future[Int] = db.run(files.size.result)
}