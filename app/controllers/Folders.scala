package controllers

import api._
import api.ApiError._
import api.JsonCombinators._
import models.FolderFake
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.i18n.Langs
import play.api.libs.json.JsValue

class Folders @Inject() (l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(l, mcc) {

  def list(sort: Option[String], p: Int, s: Int): Action[Unit] = SecuredApiAction { implicit request =>
    sortedPage(sort, FolderFake.sortingFields, default = "order") { sortingFields =>
      FolderFake.page(request.userId, sortingFields, p, s)
    }
  }

  // Returns the Location header, but not the folder information within the content body.
  def insert: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[FolderFake] { folder =>
      FolderFake.insert(request.userId, folder.name).flatMap {
        case (id, _) => created(Api.locationHeader(routes.Folders.info(id)))
      }
    }
  }

  def info(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(FolderFake.findById(id))
  }

  def update(id: Long): Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[FolderFake] { folder =>
      FolderFake.basicUpdate(id, folder.name).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  def updateOrder(id: Long, newOrder: Int): Action[Unit] = SecuredApiAction { implicit request =>
    FolderFake.updateOrder(id, newOrder).flatMap { isOk =>
      if (isOk) noContent() else errorInternal
    }
  }

  def delete(id: Long): Action[Unit] = SecuredApiAction { implicit request =>
    FolderFake.delete(id).flatMap { _ =>
      noContent()
    }
  }

}