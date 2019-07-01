package controllers

import api._
import dao.UserDAO
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs

class UsersController @Inject()(override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(dbc, l, mcc) {

  val userDao = new UserDAO(dbc)

  def usernames(): Action[Unit] = ApiAction { implicit request =>
    userDao.list.flatMap { list =>
      ok(list.map(u => Json.obj("id" -> u.id, "name" -> u.name)))
    }
  }

}
