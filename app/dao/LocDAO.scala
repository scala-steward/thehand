package dao

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.Inject
import models.{DatabaseSuffix, LocFile, FileCount}
import slick.jdbc.JdbcProfile

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

trait LocFileComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class LocFilesTable(tag: Tag, suffix: DatabaseSuffix) extends Table[LocFile](tag, suffix.suffix + "LOC_FILES") {
    def fileRef: Rep[Long] = column[Long]("file_ref")
    def count: Rep[Long] = column[Long]("count")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (fileRef, count, id) <> ((LocFile.apply _).tupled, LocFile.unapply)
  }
}

class LocDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends LocFileComponent
  with EntryFileComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private def fileQuery(suffix: DatabaseSuffix, file: FileCount) = {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    lazy val filesLocs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))
    for {
      f <- files.filter(_.path === file.path).map(_.id).result.headOption
      l <- filesLocs.filter(_.fileRef === f).map(_.id).result.headOption
      u <- updateInsert(suffix, f, l, file.lines)
    } yield u
  }

  private def updateInsert(suffix: DatabaseSuffix, fileRef: Option[Long], locId: Option[Long], count: Long) = {
    lazy val filesLocs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))
    (fileRef, locId) match {
      case (Some(fileId), Some(id)) => filesLocs.insertOrUpdate(LocFile(fileId, count, id))
      case (Some(fileId), None) => filesLocs += LocFile(fileId, count)
      case (None, _) => DBIO.successful(0)
    }
  }

  private def fileCount(file: NodeSeq): Long = (file \\ "metrics" \\ "metric").head.text.toLong

  private def parser(xml: NodeSeq) : Seq[FileCount] =
    (xml \\ "sourcemonitor_metrics" \\ "project" \\ "checkpoints" \\ "files" \\ "file")
      .map(file => FileCount(file.attribute("file_name").toString(), fileCount(file)))

  def update(suffix: DatabaseSuffix, xml: NodeSeq): Future[immutable.Seq[Int]] = {
    insert(parser(xml), suffix)
  }

  def insert(cs: Seq[FileCount], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    DBIO.sequence(cs.map(fileQuery(suffix, _))).transactionally
  }

  def list(suffix: DatabaseSuffix): Future[Seq[LocFile]] = db.run {
    lazy val filesLocs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))
    filesLocs.result
  }
}
