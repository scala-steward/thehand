package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import dao.PhaseDAO
import models.Phase
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.libs.json.JsValue

class PhasesController @Inject() (override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(dbc, l, mcc) {

  val phaseDao = new PhaseDAO(dbc)

  def list(sort: Option[String], p: Int, s: Int): Action[Unit] = SecuredApiAction { implicit request =>
    sortedPage(sort, phaseDao.sortingFields, default = "order") { sortingFields =>
      phaseDao.page(request.userId, sortingFields, p, s)
    }
  }

  // Returns the Location header, but not the folder information within the content body.
  def insert: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Phase] { folder =>
      phaseDao.insert(request.userId, folder.name).flatMap {
        case (id) => created(Api.locationHeader(routes.PhasesController.info(id)))
      }
    }
  }

  def info(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(phaseDao.findById(id))
  }

  def update(id: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[Phase] { folder =>
      phaseDao.basicUpdate(id, folder.name).flatMap { isOk =>
        if (isOk != 0) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Int): Action[Unit] = SecuredApiAction { implicit request =>
    phaseDao.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk != 0) noContent() else errorInternal
    }
  }

  def delete(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    phaseDao.delete(id).flatMap { _ =>
      noContent()
    }
  }

}