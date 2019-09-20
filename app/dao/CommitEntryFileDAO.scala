package dao

import slick.dbio.DBIOAction
import javax.inject.Inject
import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait CommitEntryFileComponent extends CommitComponent with EntryFileComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class CommitEntryFileTable(tag: Tag, suffix: DatabaseSuffix) extends Table[CommitEntryFile](tag, suffix.suffix + "COMMIT_FILES") {
    def typeModification: Rep[Option[Int]] = column[Option[Int]]("typeModification")
    def copyPathId: Rep[Option[Long]] = column[Option[Long]]("copyPath_id")
    def copyRevisionId: Rep[Option[Long]] = column[Option[Long]]("copyRevision")
    def pathId: Rep[Long] = column[Long]("path_id")
    def revisionId: Rep[Long] = column[Long]("revision")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (typeModification, copyPathId, copyRevisionId, pathId, revisionId, id) <> ((CommitEntryFile.apply _).tupled, CommitEntryFile.unapply)
    def revision = foreignKey("revision_fk", revisionId, TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
    def path = foreignKey("path_fk", pathId, TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.Cascade)
  }
}

class CommitEntryFileDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CommitEntryFileComponent
  with CommitComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(es: Seq[(Seq[CommitEntryWriter], Long)], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    def fileQuery(fileEntries: (Seq[CommitEntryWriter], Long)) = {
      val (entryFiles, revisionNumber) = fileEntries

      def tryFindFileId(path: Option[String]): DBIO[Option[Long]] = path match {
        case Some(p) => if (p == null) DBIOAction.successful(Some(-1)) else files.filter(_.path === p).map(_.id).result.headOption
        case None => DBIOAction.successful(Some(-1))
      }

      def updateInsert(c: CommitEntryWriter, id: Option[Long], commitId: Long, copyFileId: Option[Long], fileId: Option[Long]) = {
        if (id.isEmpty)
          commitsFiles += c.commit.copy(copyPath = copyFileId, pathId = fileId.get, revisionId = commitId)
        else
          commitsFiles.insertOrUpdate(c.commit.copy(copyPath = copyFileId, pathId = fileId.get, revisionId = commitId, id = id.head))
      }

      def insertFilePath(c: CommitEntryWriter, commitId: Long) = for {
        fileId <- tryFindFileId(Some(c.path))
        copyFileId <- tryFindFileId(Some(c.pathCopy))
        id <- commitsFiles.filter(_.revisionId === commitId).filter(_.pathId === fileId).map(_.id).result.headOption
        u <- updateInsert(c, id, commitId, copyFileId, fileId).asTry
      } yield u

      def insert(files: Seq[CommitEntryWriter]) = for {
        commitId <- commits.filter(_.revision === revisionNumber).map(_.id).result.headOption
        _ <- DBIO.sequence(files.map(insertFilePath(_, commitId.getOrElse(-1))))
      } yield files.size

      insert(entryFiles)
    }

    DBIO.sequence(es.map(fileQuery)).transactionally
  }

  def list(suffix: DatabaseSuffix): Future[Seq[CommitEntryFile]] = db.run {
    lazy val commits = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    commits.result
  }

  def info(suffix: DatabaseSuffix, id: Long): Future[Seq[CommitEntryFile]] = db.run {
    lazy val commits = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    commits.filter(_.id === id).result
  }
}