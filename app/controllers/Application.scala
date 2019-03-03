package controllers

import api.ApiController
import play.api.mvc._
import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.i18n.{ Langs, Messages, MessagesApi }

class Application @Inject() (langs: Langs, mcc: MessagesControllerComponents)
  extends ApiController(langs, mcc) {

  def test = ApiAction { implicit request =>
    ok("The API is ready")
  }

  // Auxiliar to check the FakeDB information. It's only for testing purpose. You should remove it.
  def fakeDB = Action { implicit request =>
    Ok(views.html.fakeDB())
  }

}
