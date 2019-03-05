package models

import play.api.libs.json.{ Json, OFormat }

case class User(
  email: String,
  password: String,
  name: String,
  emailConfirmed: Boolean,
  active: Boolean,
  id: Long = 0L)

object User {
  implicit val taskManagerFormat: OFormat[User] = Json.format[User]
}