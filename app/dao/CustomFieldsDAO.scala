package dao

import javax.inject.Inject
import models.{CustomFields, DatabaseSuffix}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait CustomFieldsComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class CustomFieldsTable(tag: Tag, suffix: DatabaseSuffix) extends Table[CustomFields](tag, suffix.suffix + "CUSTOM_FIELDS") {
    def field_value: Rep[Option[String]] = column[Option[String]]("field_value")
    def field: Rep[String] = column[String]("field")
    def taskId: Rep[Long] = column[Long]("task_id", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (field_value, field, taskId, id) <> ((CustomFields.apply _).tupled, CustomFields.unapply)
  }
}

class CustomFieldsDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CustomFieldsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(ts: Seq[CustomFields], suffix: DatabaseSuffix): Future[Seq[Int]] = db.run {
    val customs = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    def updateInsert(cf: CustomFields, customId: Option[Long]) =
      customId match {
        case Some(id) => customs.insertOrUpdate(cf.copy(id = id))
        case None => customs += cf
      }

    def customQuery(cf: CustomFields) = {
      for {
        customId <- customs.filter(_.taskId === cf.taskId).map(_.id).result.headOption
        u <- updateInsert(cf, customId)
      } yield u
    }
    DBIO.sequence(ts.map(customQuery)).transactionally
  }

  def list(suffix: DatabaseSuffix): Future[Seq[CustomFields]] = db.run {
    val custom = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    custom.sortBy(_.id).result
  }

  def listField(suffix: DatabaseSuffix, field: String): Future[Seq[CustomFields]] = db.run {
    val custom = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    custom.filter(_.field === field).sortBy(_.id).result
  }

  def info(suffix: DatabaseSuffix, id: Long): Future[Seq[CustomFields]] = db.run {
    val custom = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    custom.filter(_.id === id).result
  }
}