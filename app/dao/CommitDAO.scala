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

  class CommitTable(tag: Tag, suffix: Suffix) extends Table[CommitEntry](tag, suffix + "commits") {
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
class CommitDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, suffix: Suffix)
  extends CommitComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
  private val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))

  def listCommit_s: Future[Seq[CommitEntry]] = db.run {
    commits.result
  }

  def insert(cs: Seq[(CommitEntry, String)]): Future[Seq[Int]] = db.run {
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

  def countCommits: Future[Int] = db.run(commits.size.result)

  def actionLatestRevision(): Future[Option[Long]] =
    db.run(commits.map(_.revision).max.result)

  def latestId: Long = Await.result(actionLatestRevision(), 10 seconds) match {
    case Some(s) => s
    case None => 1L
  }

}