package controllers

import api.ApiController
import api.ApiError._
import api.JsonCombinators._
import models.User
import play.api.mvc._
import dao.UserDAO

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.i18n.Langs

import scala.concurrent.ExecutionContext

class AccountController @Inject()
(override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
(implicit executionContext: ExecutionContext)
  extends ApiController(dbc, l, mcc) {

  val userDao = new UserDAO(dbc)

  def info: Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(userDao.findById(request.userId))
  }

  def update: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[User] { user =>
      userDao.update(request.userId, user.name).flatMap { isOk =>
        if (isOk != 0) noContent() else errorInternal
      }
    }
  }

  implicit val pwdsReads: Reads[(String, String)] =
    (__ \ "old").read[String](Reads.minLength[String](1)) and
      (__ \ "new").read[String](Reads.minLength[String](6)) tupled

  def updatePassword: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[(String, String)] {
      case (oldPwd, newPwd) =>
        userDao.findById(request.userId).flatMap {
          case None => errorUserNotFound
          case Some(user) if oldPwd != user.password => errorCustom("api.error.reset.pwd.old.incorrect")
          case Some(_) => userDao.updatePassword(request.userId, newPwd).flatMap { isOk =>
            if (isOk != 0) noContent() else errorInternal
          }
        }
    }
  }

  def delete: Action[Unit] = SecuredApiAction { implicit request =>
    apiTokenDao.delete(request.token).flatMap { _ =>
      userDao.delete(request.userId).flatMap { _ =>
        noContent()
      }
    }
  }

}