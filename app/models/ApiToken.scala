package models

import org.joda.time.DateTime
import play.api.libs.json._

case class ApiToken(
  token: String, // UUID 36 digits
  apiKey: String,
  expirationTime: DateTime,
  userId: Long,
  id: Long = 0L) {

  def isExpired: Boolean = {
    expirationTime.isBeforeNow
  }
}

object ApiToken {
  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd HH:mm:ss")
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
  implicit val taskManagerFormat: OFormat[ApiToken] = Json.format[ApiToken]
}
