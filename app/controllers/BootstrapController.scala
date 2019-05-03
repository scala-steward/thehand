package controllers

import javax.inject._
import dao._
import models.Suffix
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

  def createTables(suffix: String): Action[AnyContent] = Action {
    val s = Suffix(suffix)
    dao.createSchemas(s)
    Ok("run")
  }
}