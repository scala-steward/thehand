package api

import java.time.LocalDate

import models._
import java.util.Date

import play.api.libs.json._
import play.api.libs.json.Reads.{ DefaultDateReads => _, _ }
import play.api.libs.functional.syntax._

/*
* Set of every Writes[A] and Reads[A] for render and parse JSON objects
*/
object JsonCombinators {
  implicit val dateWrites: Writes[Date] = Writes.dateWrites("dd-MM-yyyy HH:mm:ss")

  implicit val dateReads: Reads[Date] = Reads.dateReads("dd-MM-yyyy HH:mm:ss")

  implicit val userWrites: Writes[User] = (u: User) => Json.obj(
    "email" -> u.email,
    "name" -> u.name,
    "id" -> u.id)

  implicit val userReads: Reads[User] =
    (__ \ "name").read[String](minLength[String](1)).map(name => User(null, null, name, emailConfirmed = false, active = false, 0L))

  implicit val phasesWrites: Writes[Phase] = (f: Phase) => Json.obj(
    "userId" -> f.userId,
    "order" -> f.order,
    "name" -> f.name,
    "id" -> f.id)

  implicit val phasesReads: Reads[Phase] =
    (__ \ "name").read[String](minLength[String](1)).map(name => Phase(0L, 0, name, 0L))

  implicit val termWrites: Writes[Term] = (t: Term) => Json.obj(
    "folderId" -> t.phaseId,
    "order" -> t.order,
    "text" -> t.text,
    "date" -> t.date,
    "deadline" -> t.deadline,
    "done" -> t.done,
    "id" -> t.id)

  implicit val termReads: Reads[Term] = (
    (__ \ "text").read[String](minLength[String](1)) and
    (__ \ "deadline").readNullable[LocalDate])((text, deadline) => Term(0L, 0, text, null, deadline, done = false, 0L))

}