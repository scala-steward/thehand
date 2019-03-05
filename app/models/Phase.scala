package models

import play.api.libs.json.{ Json, OFormat }

case class Phase(
  userId: Long,
  order: Long,
  name: String,
  id: Long = 0L)

object Phase {
  implicit val parserFormat: OFormat[Phase] = Json.format[Phase]
}