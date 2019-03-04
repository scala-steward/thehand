package controllers

import api.ApiError._
import api.JsonCombinators._
import models.{ ApiTokenFake, UserFake }
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.actor.ActorSystem
import api.ApiController

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject

import scala.language.postfixOps
import play.api.i18n.Langs

class Auth @Inject() (l: Langs, mcc: MessagesControllerComponents, system: ActorSystem)
  extends ApiController(l, mcc) {

  implicit val loginInfoReads: Reads[(String, String)] =
    (__ \ "email").read[String](Reads.email) and
      (__ \ "password").read[String] tupled

  def signIn: Action[JsValue] = ApiActionWithBody { implicit request =>
    readFromRequest[(String, String)] {
      case (email, pwd) =>
        UserFake.findByEmail(email).flatMap {
          case None => errorUserNotFound
          case Some(user) =>
            if (user.password != pwd) errorUserNotFound
            else if (!user.emailConfirmed) errorUserEmailUnconfirmed
            else if (!user.active) errorUserInactive
            else ApiTokenFake.create(request.apiKeyOpt.get, user.id).flatMap { token =>
              ok(Json.obj(
                "token" -> token,
                "minutes" -> 10))
            }
        }
    }
  }

  def signOut: Action[Unit] = SecuredApiAction { implicit request =>
    ApiTokenFake.delete(request.token).flatMap { _ =>
      noContent()
    }
  }

  implicit val signUpInfoReads: Reads[(String, String, UserFake)] =
    (__ \ "email").read[String](Reads.email) and
      (__ \ "password").read[String](Reads.minLength[String](6)) and
      (__ \ "user").read[UserFake] tupled

  def signUp: Action[JsValue] = ApiActionWithBody { implicit request =>
    readFromRequest[(String, String, UserFake)] {
      case (email, password, user) =>
        UserFake.findByEmail(email).flatMap {
          case Some(_ /*anotherUser*/ ) => errorCustom("api.error.signup.email.exists")
          case None => UserFake.insert(email, password, user.name).flatMap {
            case (id, user_) =>

              // Send confirmation email. You will have to catch the link and confirm the email and activate the user.
              // But meanwhile...
              system.scheduler.scheduleOnce(30 seconds) {
                UserFake.confirmEmail(id)
              }

              ok(user_)
          }
        }
    }
  }

}