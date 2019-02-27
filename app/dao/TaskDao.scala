package dao

import javax.inject.{ Inject, Singleton }

import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait TaskComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class TaskTable(tag: Tag, suffix: Suffix) extends Table[Task](tag, suffix.suffix + "task") {
    def typeTask: Rep[Option[String]] = column[Option[String]]("type_task")
    def typeTaskId: Rep[Option[Long]] = column[Option[Long]]("type_task_id")
    def timeSpend: Rep[Option[Double]] = column[Option[Double]]("time_spend")
    def parentId: Rep[Option[Long]] = column[Option[Long]]("parent_id")
    def taskId: Rep[Long] = column[Long]("task_id", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (typeTask, typeTaskId, timeSpend, parentId, taskId, id) <> ((Task.apply _).tupled, Task.unapply)
  }
}

@Singleton()
class TaskDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, suffix: Suffix)
  extends TaskComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))

  def insert(ts: Seq[Task]): Future[Seq[Int]] = db.run {
    def upsert(task: Task, taskId: Option[Long]) =  {
      if (taskId.isEmpty) tasks += task else tasks.insertOrUpdate(task.copy(id = taskId.head))
    }

    def taskQuery(task: Task) = {
      for {
        taskId <- tasks.filter(_.taskId === task.taskId).map(_.id).result.headOption
        u <- upsert(task, taskId)//.asTry
      } yield u
    }

    DBIO.sequence(ts.map(taskQuery)).transactionally
  }

  def countTasks: Future[Int] = db.run(tasks.size.result)
}