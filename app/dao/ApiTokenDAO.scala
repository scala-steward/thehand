package dao

import java.util.UUID

import javax.inject.{ Inject, Singleton }
import models._
import org.joda.time.DateTime
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait ApiTokenComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  @Singleton
  class ApiTokenTable(tag: Tag) extends Table[ApiToken](tag, "API_TOKENS") {
    object PortableJodaSupport extends com.github.tototoshi.slick.GenericJodaSupport(dbConfig.profile)
    import PortableJodaSupport._

    def token: Rep[String] = column[String]("token", O.Unique)
    def apiKey: Rep[String] = column[String]("api_key")
    def expirationTime: Rep[DateTime] = column[DateTime]("expiration_time")
    def userId: Rep[Long] = column[Long]("user_id")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (token, apiKey, expirationTime, userId, id) <> ((ApiToken.apply _).tupled, ApiToken.unapply)
  }
}

class ApiTokenDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ApiTokenComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val tokens = TableQuery[ApiTokenTable]((tag: Tag) => new ApiTokenTable(tag))

  def findByTokenAndApiKey(token: String, apiKey: String): Future[Option[ApiToken]] = db.run {
    tokens.filter(t => t.token === token).filter(t => t.apiKey === apiKey).result.headOption
  }

  private def tokenExist(token: String): Future[Boolean] = db.run {
    tokens.filter(t => t.token === token).map(_.token).exists.result
  }

  private def newUUID: Future[String] = {
    val uuid = UUID.randomUUID().toString
    def selectUUID(exist: Boolean): Future[String] = {
      if (exist) newUUID
      else Future.successful(uuid)
    }
    tokenExist(uuid).flatMap(e => selectUUID(e))
  }

  private def insert(token: String, apiKey: String, userId: Long) = {
    val expirationTime = new DateTime() plusMinutes 10
    tokens += ApiToken(token, apiKey, expirationTime, userId)
  }

  def create(apiKey: String, userId: Long): Future[Int] = {
    newUUID.flatMap(token => db.run {
      insert(token, apiKey, userId)
    })
  }

  def delete(token: String): Future[Int] = db.run {
    val toDelete = for {
      deleteToken <- tokens.filter(_.token === token)
    } yield deleteToken
    toDelete.delete
  }

}