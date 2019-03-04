package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import java.util.Date

import models.TaskFake
import play.api.i18n.Langs
import play.api.libs.json.JsValue

class Tasks @Inject() (l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(l, mcc) {

  def list(folderId: Long, q: Option[String], done: Option[Boolean], sort: Option[String], p: Int, s: Int): Action[Unit] = SecuredApiAction { implicit request =>
    sortedPage(sort, TaskFake.sortingFields, default = "order") { sortingFields =>
      TaskFake.page(folderId, q, done, sortingFields, p, s)
    }
  }

  // Returns the task information within the content body, but not the Location header.
  def insert(folderId: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[TaskFake] { task =>
      TaskFake.insert(folderId, task.text, new Date(), task.deadline).flatMap {
        case (_, newTask) => created(newTask)
      }
    }
  }

  def info(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(TaskFake.findById(id))
  }

  def update(id: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[TaskFake] { task =>
      TaskFake.basicUpdate(id, task.text, task.deadline).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Int): Action[Unit] = SecuredApiAction { implicit request =>
    TaskFake.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def updateFolder(id: Long, folderId: Long): Action[Unit] = SecuredApiAction { implicit request =>
    TaskFake.updateFolder(id, folderId).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def updateDone(id: Long, done: Boolean): Action[Unit] = SecuredApiAction { implicit request =>
    TaskFake.updateDone(id, done).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def delete(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    TaskFake.delete(id).flatMap { _ =>
      noContent()
    }
  }

}