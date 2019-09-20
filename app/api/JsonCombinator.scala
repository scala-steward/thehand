package api

import models._
import java.util.Date

import play.api.libs.json._
import play.api.libs.json.Reads.{ DefaultDateReads => _, _ }

/*
* Set of every Writes[A] and Reads[A] for render and parse JSON objects
*/
object JsonCombinator {
  implicit val dateWrites: Writes[Date] = Writes.dateWrites("dd-MM-yyyy HH:mm:ss")

  implicit val dateReads: Reads[Date] = Reads.dateReads("dd-MM-yyyy HH:mm:ss")

  implicit val userWrites: Writes[User] = (u: User) => Json.obj(
    "email" -> u.email,
    "name" -> u.name,
    "id" -> u.id)

  implicit val userReads: Reads[User] =
    (__ \ "name").read[String](minLength[String](1)).map(name => User(null, null, name, emailConfirmed = false, active = false))

}