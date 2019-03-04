package controllers

import api.ApiController
import play.api.mvc._
import javax.inject.Inject
import play.api.i18n.Langs

class Application @Inject() (l: Langs, mcc: MessagesControllerComponents)
  extends ApiController(l, mcc) {

  def test: Action[Unit] = ApiAction { implicit request =>
    ok("The API is ready")
  }

  // Aux to check the FakeDB information. It's only for testing purpose. You should remove it.
  def fakeDB = Action {
    Ok(views.html.fakeDB())
  }

}
