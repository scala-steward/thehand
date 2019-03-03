package controllers

import javax.inject._
import dao._
import models.Suffix
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class BootstrapController @Inject() (
  dao: BootstrapDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def createDefaultTables(): Action[AnyContent] = Action {
    dao.createSchemas()
    Ok("run")
  }
}