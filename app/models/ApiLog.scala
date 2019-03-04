package models

import play.api.libs.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ApiLog(
  date: DateTime,
  ip: String,
  apiKey: Option[String],
  token: Option[String],
  method: String,
  uri: String,
  requestBody: Option[String],
  responseStatus: Int,
  responseBody: Option[String],
  id: Long = 0L) {

  def dateStr: String = ApiLog.dateTimeFormat.print(date)
}

object ApiLog {
  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd HH:mm:ss")
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
  implicit val taskManagerFormat: OFormat[ApiLog] = Json.format[ApiLog]

  private val dateTimeFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:ss:mm")
}

