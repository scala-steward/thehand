package models

import play.api.libs.json.{ Json, OFormat }

import scala.concurrent.Future

/*
* Stores the Api Key information
*/
case class ApiKeyFake(
  apiKey: String,
  name: String,
  active: Boolean)

object ApiKeyFake {
  import FakeDB.apiKeys

  def isActive(apiKey: String): Future[Option[Boolean]] = Future.successful {
    apiKeys.find(_.apiKey == apiKey).map(_.active)
  }

  implicit val taskManagerFormat: OFormat[ApiKeyFake] = Json.format[ApiKeyFake]

}
