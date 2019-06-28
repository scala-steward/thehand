package controllers

import api._
import api.ApiError._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject

import dao.TermDAO
import models.Term
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.libs.json.JsValue

class TermController @Inject() (override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(dbc, l, mcc) {

  val termDao = new TermDAO(dbc)

  def list(folderId: Long, done: Option[Boolean], sort: Option[String], p: Int, s: Int): Action[Unit] = SecuredApiAction { implicit request =>
    sortedPage(sort, termDao.sortingFields, default = "order") { sortingFields =>
      termDao.page(folderId, done, sortingFields, p, s)
    }
  }

  // Returns the task information within the content body, but not the Location header.
  def insert(folderId: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Term] { term =>
      termDao.insert(folderId, term.text, java.time.LocalDate.now(), term.deadline)
        .flatMap(newTerm => created(newTerm))
    }
  }

  def info(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(termDao.findById(id))
  }

  def update(id: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Term] { t =>
      termDao.basicUpdate(id, t.text, t.deadline).flatMap { isOk =>
        if (isOk != 0) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Long): Action[Unit] = SecuredApiAction { implicit request =>
    termDao.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk != 0) noContent() else errorInternal
    }
  }

  def updateFolder(id: Long, folderId: Long): Action[Unit] = SecuredApiAction { implicit request =>
    termDao.updatePhase(id, folderId).flatMap { isOk =>
      if (isOk != 0) noContent() else errorInternal
    }
  }

  def updateDone(id: Long, done: Boolean): Action[Unit] = SecuredApiAction { implicit request =>
    termDao.updateDone(id, done).flatMap { isOk =>
      if (isOk != 0) noContent() else errorInternal
    }
  }

  def delete(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    termDao.delete(id).flatMap { _ =>
      noContent()
    }
  }

}