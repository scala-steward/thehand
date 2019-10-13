package dao

import javax.inject.Inject

import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait AuthorComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class AuthorsTable(tag: Tag, suffix: DatabaseSuffix) extends Table[Author](tag, suffix.suffix + "AUTHORS") {
    def author: Rep[String] = column[String]("author", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (author, id) <> ((Author.apply _).tupled, Author.unapply)
  }
}

class AuthorDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AuthorComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def list(suffix: DatabaseSuffix): Future[Seq[Author]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    authors.sortBy(_.id).result
  }

  def insert(as: Seq[Author], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))

    def updateInsert(author: Author, authorId: Option[Long]) =
      authorId match {
        case Some(id) => authors.insertOrUpdate(author.copy(author.author,id))
        case None => authors += author
      }

    def authorQuery(author: Author) = {
      for {
        authorId <- authors.filter(_.author === author.author).map(_.id).result.headOption
        u <- updateInsert(author, authorId)
      } yield u
    }

    DBIO.sequence(as.map(authorQuery)).transactionally
  }

  def info(suffix: DatabaseSuffix, id: Long): Future[Option[Author]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    authors.filter(_.id === id).result.headOption
  }
}
