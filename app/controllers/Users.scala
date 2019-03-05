package controllers

import api._
import models.UserFake
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs

class Users @Inject() (override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(dbc, l, mcc) {

  def usernames: Action[Unit] = ApiAction { implicit request =>
    UserFake.list.flatMap { list =>
      ok(list.map(u => Json.obj("id" -> u.id, "name" -> u.name)))
    }
  }

}
