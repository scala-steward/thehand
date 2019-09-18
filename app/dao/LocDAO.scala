package dao

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.Inject
import models.{DatabaseSuffix, LocFile}
import slick.jdbc.JdbcProfile
import telemetrics.HandLogger

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

trait LocFileComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class LocFilesTable(tag: Tag, suffix: DatabaseSuffix) extends Table[LocFile](tag, suffix.suffix + "LOC_FILES") {
    def fileRef: Rep[Long] = column[Long]("file_ref", O.Unique)
    def count: Rep[Long] = column[Long]("count", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (fileRef, count, id) <> ((LocFile.apply _).tupled, LocFile.unapply)
  }
}

class LocDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends LocFileComponent
  with EntryFileComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  case class FileCount(path : String, lines: Long)

  private def parser(xml: NodeSeq) = {
    val files_xml = xml \\ "sourcemonitor_metrics" \\ "project" \\ "checkpoints" \\ "files" \\ "file"
    files_xml.map(file => FileCount("/trunk/" + file.attribute("file_name").get.toString(), (file \\ "metrics" \\ "metric").head.text.toLong)) //HIRO remover o trunk
  }

  def update(suffix: DatabaseSuffix, xml: NodeSeq): Future[Seq[Int]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    lazy val locs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))

    val filesCount = parser(xml)
    HandLogger.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> FilesCount " + filesCount.size)

    def updateInsert(file: LocFile, id: Option[Long]) = {
      if (id.isEmpty) locs += file else locs.insertOrUpdate(file.copy(id = id.head))
    }

    def fileQuery(file: FileCount) = {
      for {
        fileId <- files.filter(_.path === file.path).map(_.id).result.headOption
        locId <- locs.filter(_.fileRef === fileId).map(_.id).result.headOption
        u <- updateInsert(LocFile(fileId.getOrElse(0), file.lines), locId)
      } yield u
    }

    DBIO.sequence(filesCount.map(fileQuery)).transactionally
  }
}
