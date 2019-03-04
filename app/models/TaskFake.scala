package models

import java.util.Date

import api.Api.Sorting._
import api.Page
import play.api.libs.json.{ Json, OFormat }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class TaskFake(
  id: Long,
  folderId: Long,
  order: Int,
  text: String,
  date: Date,
  deadline: Option[Date],
  done: Boolean)

object TaskFake {
  import FakeDB.tasks

  implicit val parserFormat: OFormat[TaskFake] = Json.format[TaskFake]

  private def lastInFolder(folderId: Long): Int = (-1 +: tasks.filter(_.folderId == folderId).map(_.order)).max

  def findById(id: Long): Future[Option[TaskFake]] = Future.successful {
    tasks.get(id)
  }

  def insert(folderId: Long, text: String, date: Date, deadline: Option[Date]): Future[(Long, TaskFake)] = Future.successful {
    tasks.insert(TaskFake(_, folderId, order = lastInFolder(folderId) + 1, text, date, deadline, done = false))
  }

  def basicUpdate(id: Long, text: String, deadline: Option[Date]): Future[Boolean] = Future.successful {
    tasks.update(id)(_.copy(text = text, deadline = deadline))
  }
  def updateOrder(id: Long, order: Int): Future[Boolean] = Future.successful {
    tasks.update(id) { task =>
      val newOrder = Math.max(0, Math.min(lastInFolder(task.folderId), order))
      val oldOrder = task.order
      if (newOrder == oldOrder)
        task
      else {
        if (newOrder > oldOrder) {
          tasks.filter(t => t.folderId == task.folderId && t.order > oldOrder && t.order <= newOrder).map { t =>
            tasks.update(t.id)(_.copy(order = t.order - 1))
          }
        } else if (newOrder < oldOrder) {
          tasks.filter(t => t.folderId == task.folderId && t.order >= newOrder && t.order < oldOrder).map { t =>
            tasks.update(t.id)(_.copy(order = t.order + 1))
          }
        }
        task.copy(order = newOrder)
      }
    }
  }
  def updateFolder(id: Long, folderId: Long): Future[Boolean] = updateOrder(id, Int.MaxValue).map { hasUpdated =>
    if (hasUpdated)
      tasks.update(id)(_.copy(folderId = folderId, order = lastInFolder(folderId) + 1))
    else false
  }
  def updateDone(id: Long, done: Boolean): Future[Boolean] = Future.successful {
    tasks.update(id)(_.copy(done = done))
  }

  def delete(id: Long): Future[Unit] = updateOrder(id, Int.MaxValue).map { hasUpdated =>
    if (hasUpdated)
      tasks.delete(id)
  }

  //////////////////////////////////////////////////
  // PAGINATION utilities

  /*
	* Returns a Page[Task] with the user's tasks
	* - sortFields: list of sorting params indicating their fields and if it should be ordered in ascending or descending order. Ex: Seq(("+", "order"), ("-", "date"))
	*/
  def page(folderId: Long, query: Option[String], done: Option[Boolean], sortingFields: Seq[(String, Boolean)], p: Int, s: Int): Future[Page[TaskFake]] = Future.successful {
    val filterFunc: TaskFake => Boolean = { task =>
      task.folderId == folderId &&
        query.map(q => task.text.toLowerCase.contains(q.toLowerCase)).getOrElse(true) &&
        done.map(task.done == _).getOrElse(true)
    }
    tasks.page(p, s)(filterFunc)(sortingFields.map(sortingFunc): _*)
  }

  // List with all the available sorting fields.
  val sortingFields = Seq("id", "order", "date", "deadline", "done")
  // Defines a sorting function for the pair (field, order)
  def sortingFunc(fieldsWithOrder: (String, Boolean)): (TaskFake, TaskFake) => Boolean = fieldsWithOrder match {
    case ("id", ASC) => _.id < _.id
    case ("id", DESC) => _.id > _.id
    case ("order", ASC) => _.order < _.order
    case ("order", DESC) => _.order > _.order
    case ("date", ASC) => _.date before _.date
    case ("date", DESC) => _.date after _.date
    case ("deadline", ASC) => (a, b) => a.deadline.map(ad => b.deadline.forall(bd => ad before bd)).getOrElse(false)
    case ("deadline", DESC) => (a, b) => a.deadline.map(ad => b.deadline.map(bd => ad after bd).getOrElse(true)).getOrElse(false)
    case ("done", ASC) => _.done > _.done
    case ("done", DESC) => _.done < _.done
    case _ => (_, _) => false
  }

}