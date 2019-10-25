package dao

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.Inject
import models.{DatabaseSuffix, FileCount, LocFile}
import play.api.libs.json.JsValue
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
      f <- files.filter(_.path.toLowerCase === file.path.toLowerCase()).map(_.id).result.headOption
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

  private def parserXml(xml: NodeSeq) : Seq[FileCount] =
    (xml \\ "sourcemonitor_metrics" \\ "project" \\ "checkpoints" \\ "files" \\ "file")
      .map(file => FileCount(file.attribute("file_name").getOrElse("").toString(), fileCount(file)))

  def updateXml(suffix: DatabaseSuffix, xml: NodeSeq): Future[immutable.Seq[Int]] = {
    insert(parserXml(xml), suffix)
  }

  def update(suffix: DatabaseSuffix, json: JsValue): Future[immutable.Seq[Int]] = {
    insert(parser(json), suffix)
  }

  def insert(cs: Seq[FileCount], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    DBIO.sequence(cs.map(fileQuery(suffix, _))).transactionally
  }

  def list(suffix: DatabaseSuffix): Future[Seq[LocFile]] = db.run {
    lazy val filesLocs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))
    filesLocs.result
  }

  private def processRequest(request: collection.Seq[JsValue]): Seq[FileCount]  =
    request.map {
      js =>
        val path = (js \ "path").validate[String].getOrElse("")
        val counter = (js \ "counter").validate[Long].getOrElse(0L)
        FileCount(path, counter)
    }.toSeq

  private def parser(json: JsValue) : Seq[FileCount] = {
    json.validateOpt[collection.Seq[JsValue]].getOrElse(None) match {
      case Some(request) => processRequest(request)
      case _ => Seq()
    }
  }
}
