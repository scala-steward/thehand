package models

import play.api.libs.json.{ Json, OFormat }

import scala.concurrent.Future

case class UserFake(
  id: Long,
  email: String,
  password: String,
  name: String,
  emailConfirmed: Boolean,
  active: Boolean)

object UserFake {
  import FakeDB.users

  implicit val taskManagerFormat: OFormat[UserFake] = Json.format[UserFake]

  def findById(id: Long): Future[Option[UserFake]] = Future.successful {
    users.get(id)
  }
  def findByEmail(email: String): Future[Option[UserFake]] = Future.successful {
    users.find(_.email == email)
  }

  def insert(email: String, password: String, name: String): Future[(Long, UserFake)] = Future.successful {
    users.insert(UserFake(_, email, password, name, emailConfirmed = false, active = false))
  }

  def update(id: Long, name: String): Future[Boolean] = Future.successful {
    users.update(id)(_.copy(name = name))
  }

  def confirmEmail(id: Long): Future[Boolean] = Future.successful {
    users.update(id)(_.copy(emailConfirmed = true, active = true))
  }

  def updatePassword(id: Long, password: String): Future[Boolean] = Future.successful {
    users.update(id)(_.copy(password = password))
  }

  def delete(id: Long): Future[Unit] = Future.successful {
    FakeDB.folders.map(f => FakeDB.tasks.delete(_.folderId == f.id))
    FakeDB.folders.delete(_.userId == id)
    users.delete(id)
  }

  def list: Future[Seq[UserFake]] = Future.successful {
    users.values.sortBy(_.name)
  }

}
