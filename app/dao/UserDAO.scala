package dao

import javax.inject.{ Inject, Singleton }
import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait UserComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  @Singleton
  class UserTable(tag: Tag) extends Table[User](tag, "USERS") {
    def email = column[String]("email", O.Unique)
    def password = column[String]("password")
    def name = column[String]("name")
    def emailConfirmed = column[Boolean]("emailConfirmed")
    def active = column[Boolean]("active")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (email, password, name, emailConfirmed, active, id) <> ((User.apply _).tupled, User.unapply)
  }
}

class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends UserComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val users = TableQuery[UserTable]((tag: Tag) => new UserTable(tag))

  def findById(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def findByEmail(email: String): Future[Option[User]] = db.run {
    users.filter(_.email === email).result.headOption
  }

  def insert(email: String, password: String, name: String): Future[Option[User]] = db.run {
    (users += User(email, password, name, emailConfirmed = false, active = false)) andThen
      users.filter(_.email === email).result.headOption
  }

  def confirmEmail(id: Long): Future[Int] = db.run {
    for {
      u <- users.filter(_.id === id).result.headOption
      i <- users.update(u.get.copy(emailConfirmed = true, active = true)) if u.isDefined
    } yield i
  }

  def update(id: Long, name: String): Future[Int] = db.run {
    for {
      u <- users.filter(_.id === id).result.headOption
      i <- users.update(u.get.copy(name = name)) if u.isDefined
    } yield i
  }

  def updatePassword(id: Long, password: String): Future[Int] = db.run {
    for {
      u <- users.filter(_.id === id).result.headOption
      i <- users.update(u.get.copy(password = password)) if u.isDefined
    } yield i
  }

  def delete(id: Long): Future[Unit] =
    db.run(users.filter(_.id === id).delete).map(_ => ())

  def list: Future[Seq[User]] = db.run {
    users.sortBy(_.name).result
  }
}