package models

import play.api.libs.json.{ Json, OFormat }

case class LocFile(fileRef: Long, count: Long = 0L, id: Long = 0L)

object LocFile {
  implicit val entryFormat: OFormat[LocFile] = Json.format[LocFile]
}