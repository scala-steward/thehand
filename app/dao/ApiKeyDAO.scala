package dao

import javax.inject.{ Inject, Singleton }
import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait ApiKeyComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class ApiKeyTable(tag: Tag) extends Table[ApiKey](tag, "API_KEY") {
    def apiKey: Rep[String] = column[String]("api_key", O.Unique)
    def name: Rep[String] = column[String]("name")
    def active: Rep[Boolean] = column[Boolean]("active")
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (apiKey, name, active, id) <> ((ApiKey.apply _).tupled, ApiKey.unapply)
  }
}

@Singleton
class ApiKeyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends ApiKeyComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val apiKeys = TableQuery[ApiKeyTable]((tag: Tag) => new ApiKeyTable(tag))

  def isActive(apiKey: String): Future[Option[Boolean]] = db.run {
    apiKeys.filter(_.apiKey === apiKey).map(_.active).result.headOption
  }

  def delete(id: Long): Future[Unit] =
    db.run(apiKeys.filter(_.id === id).delete).map(_ => ())
}