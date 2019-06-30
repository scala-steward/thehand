package dao

import java.sql.Timestamp

import javax.inject.Inject
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait CommitComponent extends AuthorComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class CommitTable(tag: Tag, suffix: DatabaseSuffix) extends Table[CommitEntry](tag, suffix.suffix + "COMMITS") {
    def message: Rep[Option[String]] = column[Option[String]]("message")
    def timestamp: Rep[Option[Timestamp]] = column[Option[Timestamp]]("timestamp")
    def revision: Rep[Long] = column[Long]("revision", O.Unique)
    def authorId: Rep[Long] = column[Long]("author")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (message, timestamp, revision, authorId, id) <> ((CommitEntry.apply _).tupled, CommitEntry.unapply)
    def author = foreignKey("author_fk", authorId, TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.SetNull)
  }
}

class CommitDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CommitComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def list(suffix: DatabaseSuffix): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.sortBy(_.id).result
  }

  def info(suffix: DatabaseSuffix, id: Long): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.filter(_.id === id).result
  }

  def infoRevision(suffix: DatabaseSuffix, revision: Long): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.filter(_.revision === revision).result
  }

  def infoDate(suffix: DatabaseSuffix, from: Timestamp, to: Timestamp): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.filter(_.timestamp >= from).filter(_.timestamp <= to).result
  }

  def insert(cs: Seq[(CommitEntry, String)], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    def updateInsert(commit: CommitEntry, commitId: Option[Long], authorId: Option[Long]) = {
      if (commitId.isEmpty)
        commits += commit.copy(authorId = authorId.head)
      else
        commits.insertOrUpdate(commit.copy(authorId = authorId.head, id = commitId.head))
    }

    def commitQuery(entry: (CommitEntry, String)) = {
      val (commit, authorName) = entry
      for {
        authorId <- authors.filter(_.author === authorName).map(_.id).result.headOption
        commitId <- commits.filter(_.revision === commit.revision).map(_.id).result.headOption
        u <- updateInsert(commit, commitId, authorId)
      } yield u
    }

    DBIO.sequence(cs.map(commitQuery)).transactionally
  }

  def actionLatestRevision(suffix: DatabaseSuffix): Future[Option[Long]] = {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    db.run(commits.map(_.revision).max.result)
  }
}