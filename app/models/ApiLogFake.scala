package models

import api.ApiRequestHeader
import play.api.mvc.RequestHeader
import play.api.libs.json._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.concurrent.Future

/*
* Stores all the information of a request. Specially used for store the errors in the DB.
*/
case class ApiLogFake(
  id: Long,
  date: DateTime,
  ip: String,
  apiKey: Option[String],
  token: Option[String],
  method: String,
  uri: String,
  requestBody: Option[String],
  responseStatus: Int,
  responseBody: Option[String]) {
  def dateStr: String = ApiLogFake.dtf.print(date)
}
object ApiLogFake {
  import FakeDB.logs

  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd HH:mm:ss")
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
  implicit val taskManagerFormat: OFormat[ApiLogFake] = Json.format[ApiLogFake]

  private val dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:ss:mm")

  def findById(id: Long): Future[Option[ApiLogFake]] = Future.successful {
    logs.get(id)
  }

  def insert[R <: RequestHeader](request: ApiRequestHeader[R], status: Int, json: JsValue): Future[(Long, ApiLogFake)] = Future.successful {
    logs.insert(ApiLogFake(
      _,
      date = request.dateOrNow,
      ip = request.RremoteAddress,
      apiKey = request.apiKeyOpt,
      token = request.tokenOpt,
      method = request.method,
      uri = request.Uuri,
      requestBody = request.maybeBody,
      responseStatus = status,
      responseBody = if (json == JsNull) None else Some(Json.prettyPrint(json))))
  }

  def delete(id: Long): Future[Unit] = Future.successful {
    logs.delete(id)
  }

}