package controllers

import models.DatabaseSuffix
import play.api.mvc._
import akka.actor.ActorSystem
import api.ApiController
import dao.LocDAO
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.NodeSeq

class LocController @Inject()
(override val dbc: DatabaseConfigProvider, l: Langs, mcc: MessagesControllerComponents, system: ActorSystem)
  extends ApiController(dbc, l, mcc) {

  val locDao = new LocDAO(dbc)

  def updateXml(suffix: DatabaseSuffix): Action[NodeSeq] = ApiActionWithXmlBody { implicit request =>
    val body = request.request.body
    maybeSeq(locDao.updateXml(suffix, body))
  }

  def update(suffix: DatabaseSuffix): Action[JsValue] = ApiActionWithBody { implicit request =>
    val body = request.request.body
    maybeSeq(locDao.update(suffix, body))
  }

}
