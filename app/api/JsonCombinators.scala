package api

import models.{ TaskFake, _ }
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

  implicit val userWrites: Writes[UserFake] = (u: UserFake) => Json.obj(
    "id" -> u.id,
    "email" -> u.email,
    "name" -> u.name)

  implicit val userReads: Reads[UserFake] =
    (__ \ "name").read[String](minLength[String](1)).map(name => UserFake(0L, null, null, name, emailConfirmed = false, active = false))

  implicit val folderWrites: Writes[FolderFake] = (f: FolderFake) => Json.obj(
    "id" -> f.id,
    "userId" -> f.userId,
    "order" -> f.order,
    "name" -> f.name)

  implicit val folderReads: Reads[FolderFake] =
    (__ \ "name").read[String](minLength[String](1)).map(name => FolderFake(0L, 0L, 0, name))

  implicit val taskWrites: Writes[TaskFake] = (t: TaskFake) => Json.obj(
    "id" -> t.id,
    "folderId" -> t.folderId,
    "order" -> t.order,
    "text" -> t.text,
    "date" -> t.date,
    "deadline" -> t.deadline,
    "done" -> t.done)

  implicit val taskReads: Reads[TaskFake] = (
    (__ \ "text").read[String](minLength[String](1)) and
    (__ \ "deadline").readNullable[Date])((text, deadline) => TaskFake(0L, 0L, 0, text, null, deadline, done = false))

}