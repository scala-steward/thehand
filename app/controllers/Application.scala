package controllers

import api.ApiController
import play.api.mvc._
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs

import scala.concurrent.ExecutionContext

class Application @Inject()
(override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents)
(implicit executionContext: ExecutionContext)
  extends ApiController(dbc, l, mcc) {

  def test: Action[Unit] = ApiAction { implicit request =>
    ok("The API is ready")
  }

}
