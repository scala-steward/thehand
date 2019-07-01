package controllers

import javax.inject._
import dao.BootDAO
import models.{DatabaseSuffix, QueryMagic}
import play.api.mvc._

class BootController @Inject()
(dao: BootDAO, cc: MessagesControllerComponents)
  extends MessagesAbstractController(cc) {

  def createTables(magic: QueryMagic): Action[AnyContent] = Action {
    dao.createSchemas()
    dao.createFirstApiKey()
    Accepted("boot")
  }

  def createSuffixTables(suffix: DatabaseSuffix, magic: QueryMagic): Action[AnyContent] = Action {
    dao.createSchemas(suffix)
    Accepted(s"boot ${suffix.suffix}")
  }
}