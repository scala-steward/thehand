package models

import play.api.libs.json._

final case class CustomFields(
                       fieldValue: Option[String],
                       field: String,
                       taskId: Long,
                       id: Long = 0L)

object CustomFields {
  implicit val customFiledsFormat: OFormat[CustomFields] = Json.format[CustomFields]
}

