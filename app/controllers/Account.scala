package controllers

import api.ApiController
import api.ApiError._
import api.JsonCombinators._
import models.{ ApiTokenFake, UserFake }
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.i18n.Langs

class Account @Inject() (override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(dbc, l, mcc) {

  def info: Action[Unit] = SecuredApiAction { implicit request =>
    maybeItem(UserFake.findById(request.userId))
  }

  def update: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[UserFake] { user =>
      UserFake.update(request.userId, user.name).flatMap { isOk =>
        if (isOk) noContent() else errorInternal
      }
    }
  }

  implicit val pwdsReads: Reads[(String, String)] =
    (__ \ "old").read[String](Reads.minLength[String](1)) and
      (__ \ "new").read[String](Reads.minLength[String](6)) tupled

  def updatePassword: Action[JsValue] = SecuredApiActionWithBody { implicit request =>
    readFromRequest[(String, String)] {
      case (oldPwd, newPwd) =>
        UserFake.findById(request.userId).flatMap {
          case None => errorUserNotFound
          case Some(user) if oldPwd != user.password => errorCustom("api.error.reset.pwd.old.incorrect")
          case Some(_) => UserFake.updatePassword(request.userId, newPwd).flatMap { isOk =>
            if (isOk) noContent() else errorInternal
          }
        }
    }
  }

  def delete: Action[Unit] = SecuredApiAction { implicit request =>
    ApiTokenFake.delete(request.token).flatMap { _ =>
      UserFake.delete(request.userId).flatMap { _ =>
        noContent()
      }
    }
  }

}