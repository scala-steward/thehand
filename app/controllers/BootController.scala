package controllers

import javax.inject._
import dao._
import models.DatabeSuffix
import play.api.mvc._

class BootController @Inject()(dao: BootDAO, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) {

  def createDefaultTables(): Action[AnyContent] = Action {
    dao.createSchemas()
    Accepted("boot")
  }

  def createTables(suffix: DatabeSuffix): Action[AnyContent] = Action {
    dao.createSchemas(suffix)
    Accepted(s"boot ${suffix.suffix}")
  }
}