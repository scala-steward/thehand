package dao

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import javax.inject.Inject
import models.{DatabaseSuffix, LocFile}
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

  class FileCount(val path: String, val lines: Long)
  object FileCount {
    def apply(path: String, lines: Long): FileCount = new FileCount(path.replace("\\", "/"), lines)
  }

  private def fileCount(file: NodeSeq): Long = (file \\ "metrics" \\ "metric").head.text.toLong

  private def parser(xml: NodeSeq) : Seq[FileCount] =
    (xml \\ "sourcemonitor_metrics" \\ "project" \\ "checkpoints" \\ "files" \\ "file")
      .map(file => FileCount("/trunk/" + file.attribute("file_name"), fileCount(file)))

  def update(suffix: DatabaseSuffix, xml: NodeSeq): Future[immutable.Seq[Int]] = db.run {
    lazy val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    lazy val filesLocs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))

    def updateInsert(fileRef: Option[Long], locId: Option[Long], count: Long) =
      (fileRef, locId) match {
        case (None, _) => filesLocs.size.result //HIRO dummy query
        case (Some(_), None) => filesLocs += LocFile(fileRef.get, count)
        case (Some(_), Some(id)) => filesLocs.insertOrUpdate(LocFile(fileRef.get, count, id))
      }

    def fileQuery(file: FileCount) = {
      for {
        f <- files.filter(_.path === file.path).map(_.id).result.headOption
        l <- filesLocs.filter(_.fileRef === f).map(_.id).result.headOption
        u <- updateInsert(f, l, file.lines)
      } yield u
    }

    DBIO.sequence(parser(xml).map(fileQuery)).transactionally
  }
}
