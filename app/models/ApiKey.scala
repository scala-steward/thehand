package models

import play.api.libs.json.{ Json, OFormat }

case class ApiKey(
  apiKey: String,
  name: String,
  active: Boolean,
  id: Long = 0L)

object ApiKey {
  implicit val taskManagerFormat: OFormat[ApiKey] = Json.format[ApiKey]
}
