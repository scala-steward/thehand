package dao

import api.ApiRequestHeader
import javax.inject.{ Inject, Singleton }
import models._
import org.joda.time.DateTime
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.json.{ JsNull, JsValue, Json }
import play.api.mvc.RequestHeader
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait ApiLogComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  @Singleton
  class ApiLogTable(tag: Tag) extends Table[ApiLog](tag, "API_LOG") {
    object PortableJodaSupport extends com.github.tototoshi.slick.GenericJodaSupport(dbConfig.profile)
    import PortableJodaSupport._

    def date = column[DateTime]("date")
    def ip = column[String]("ip")
    def apiKey = column[Option[String]]("api_key")
    def token = column[Option[String]]("token")
    def method = column[String]("method")
    def uri = column[String]("uri")
    def requestBody = column[Option[String]]("request_body")
    def responseStatus = column[Int]("response_status")
    def responseBody = column[Option[String]]("response_body")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (date, ip, apiKey, token, method, uri, requestBody, responseStatus, responseBody, id) <> ((ApiLog.apply _).tupled, ApiLog.unapply)
  }
}

class ApiLogDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ApiLogComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val logs = TableQuery[ApiLogTable]((tag: Tag) => new ApiLogTable(tag))

  def findById(id: Long): Future[Option[ApiLog]] = db.run {
    logs.filter(_.id === id).result.headOption
  }

  //old Future[(Long, ApiLog)]
  def insert[R <: RequestHeader](request: ApiRequestHeader[R], status: Int, json: JsValue): Future[Int] = db.run {
    logs += ApiLog(
      date = DateTime.now(),
      ip = request.RremoteAddress,
      apiKey = request.apiKeyOpt,
      token = request.tokenOpt,
      method = request.method,
      uri = request.Uuri,
      requestBody = request.maybeBody,
      responseStatus = status,
      responseBody = if (json == JsNull) None else Some(Json.prettyPrint(json)))
  }

  def delete(id: Long): Future[Unit] =
    db.run(logs.filter(_.id === id).delete).map(_ => ())
}