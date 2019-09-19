package dao

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.Inject
import models.{DatabaseSuffix, LocFile}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext}
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

  case class FileCount(path : String, lines: Long)

  private def parser(xml: NodeSeq) = {
    val files_xml = xml \\ "sourcemonitor_metrics" \\ "project" \\ "checkpoints" \\ "files" \\ "file"
    files_xml.map(file => FileCount("/trunk/" + file.attribute("file_name").get.toString().replace("\\", "/"), (file \\ "metrics" \\ "metric").head.text.toLong)) //HIRO remover o trunk
  }

  def update(suffix: DatabaseSuffix, xml: NodeSeq) = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    lazy val locs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))

    def updateInsert(fileRef: Option[Long], id: Option[Long], count: Long) = {
      if (fileRef.isEmpty) {
        locs.size.result
      }
      else {
        if (id.isEmpty) locs += LocFile(fileRef.get, count)
        else locs.insertOrUpdate(LocFile(fileRef.get, count, id.get))
      }
    }

    def fileQuery(file: FileCount) = {
      for {
        f <- files.filter(_.path === file.path).map(_.id).result.headOption
        l <- locs.filter(_.fileRef === f).map(_.id).result.headOption
        u <- updateInsert(f, l, file.lines)
      } yield u
    }

    DBIO.sequence(parser(xml).map(fileQuery)).transactionally
  }
}
