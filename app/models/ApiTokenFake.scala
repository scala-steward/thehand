package models

import org.joda.time.DateTime
import java.util.UUID

import play.api.libs.json._

import scala.concurrent.Future

/*
* Stores the Auth Token information. Each token belongs to a Api Key and user
*/
case class ApiTokenFake(
  token: String, // UUID 36 digits
  apiKey: String,
  expirationTime: DateTime,
  userId: Long) {
  def isExpired: Boolean = expirationTime.isBeforeNow
}

object ApiTokenFake {
  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd HH:mm:ss")
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
  implicit val taskManagerFormat: OFormat[ApiTokenFake] = Json.format[ApiTokenFake]

  import FakeDB.tokens

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiTokenFake]] = Future.successful {
    tokens.find(t => t.token == token && t.apiKey == apiKey)
  }

  def create(apiKey: String, userId: Long): Future[String] = Future.successful {
    // Be sure the uuid is not already taken for another token
    def newUUID: String = {
      val uuid = UUID.randomUUID().toString
      if (!tokens.exists(_.token == uuid)) uuid else newUUID
    }
    val token = newUUID
    tokens.insert(_ => ApiTokenFake(token, apiKey, expirationTime = new DateTime() plusMinutes 10, userId))
    token
  }

  def delete(token: String): Future[Unit] = Future.successful {
    tokens.delete(_.token == token)
  }
}
