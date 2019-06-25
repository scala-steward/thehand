package dao

import javax.inject.Inject
import models.{CustomFields, Suffix}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import telemetrics.HandLogger

import scala.concurrent.{ExecutionContext, Future}

trait CustomFieldsComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class CustomFiledsTable(tag: Tag, suffix: Suffix) extends Table[CustomFields](tag, suffix.suffix + "customfields") {
    def requestType: Rep[Option[String]] = column[Option[String]]("request_type")
    def taskId: Rep[Long] = column[Long]("task_id", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (requestType, taskId, id) <> ((CustomFields.apply _).tupled, CustomFields.unapply)
  }
}

class CustomFieldsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CustomFieldsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(ts: Seq[CustomFields], suffix: Suffix): Future[Seq[Int]] = db.run {
    val customs = TableQuery[CustomFiledsTable]((tag: Tag) => new CustomFiledsTable(tag, suffix))
    def updateInsert(cf: CustomFields, customId: Option[Long]) = {
      if (customId.isEmpty) customs += cf else customs.insertOrUpdate(cf.copy(id = customId.head))
    }

    def customQuery(cf: CustomFields) = {
      HandLogger.debug(cf.toString)
      for {
        customId <- customs.filter(_.taskId === cf.taskId).map(_.id).result.headOption
        u <- updateInsert(cf, customId)
      } yield u
    }
    DBIO.sequence(ts.map(customQuery)).transactionally
  }

  def countTasks(suffix: Suffix): Future[Int] = db.run {
    val tasks = TableQuery[CustomFiledsTable]((tag: Tag) => new CustomFiledsTable(tag, suffix))
    tasks.size.result
  }

  def list(suffix: Suffix): Future[Seq[CustomFields]] = db.run {
    val tasks = TableQuery[CustomFiledsTable]((tag: Tag) => new CustomFiledsTable(tag, suffix))
    tasks.result
  }
}