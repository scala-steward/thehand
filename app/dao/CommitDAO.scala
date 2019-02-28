package dao

import org.joda.time.DateTime
import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.language.postfixOps

import scala.concurrent.{Await, ExecutionContext, Future}

trait CommitComponent extends AuthorComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  object PortableJodaSupport extends com.github.tototoshi.slick.GenericJodaSupport(dbConfig.profile)
  import PortableJodaSupport._

  class CommitTable(tag: Tag, suffix: Suffix) extends Table[CommitEntry](tag, suffix.suffix + "commits") {
    def message: Rep[Option[String]] = column[Option[String]]("message")
    def date: Rep[Option[DateTime]] = column[Option[DateTime]]("date")
    def revision: Rep[Long] = column[Long]("revision", O.Unique)
    def authorId: Rep[Long] = column[Long]("author")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (message, date, revision, authorId, id) <> ((CommitEntry.apply _).tupled, CommitEntry.unapply)
    def author = foreignKey("author_fk", authorId, TableQuery[AuthorsTable]((tag:Tag) => new AuthorsTable(tag, suffix)))(_.id, onDelete = ForeignKeyAction.SetNull)
  }
}

@Singleton()
class CommitDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CommitComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def list(suffix: Suffix): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.result
  }

  def list(suffix: Suffix, revision: Option[Long]): Future[Seq[CommitEntry]] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    revision match {
      case Some(revision) => commits.filter(_.revision === revision).result
      case None => commits.result
    }
  }

  def insert(cs: Seq[(CommitEntry, String)], suffix: Suffix): Future[Seq[Int]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    def upsert(commit: CommitEntry, commitId: Option[Long], authorId: Option[Long]) = {
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
        u <- upsert(commit, commitId, authorId)//.asTry
      } yield u
    }

    DBIO.sequence(cs.map(commitQuery)).transactionally
  }

  def countCommits(suffix: Suffix): Future[Int] = db.run {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    commits.size.result
  }

  def actionLatestRevision(suffix: Suffix): Future[Option[Long]] = {
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    db.run(commits.map(_.revision).max.result)
  }
}