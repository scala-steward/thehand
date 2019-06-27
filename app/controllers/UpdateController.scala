/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers

import api.ApiController
import javax.inject._
import dao._
import models.Suffix
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.mvc._

import scala.concurrent.ExecutionContext

class UpdateController @Inject() (override val dbc: DatabaseConfigProvider, dao: UpdateDAO, l: Langs, mcc: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends ApiController(dbc, l, mcc) {

  def unsafeUpdateAll(): Action[AnyContent] = Action {
    dao.updateAll()
    Ok("Updated All")
  }

  def updateAll(): Action[Unit] = SecuredApiAction { implicit request =>
    dao.updateAll().flatMap { _ => noContent() }
  }

  def update(suffix: String, from: Option[Long], to: Option[Long]): Action[Unit] = SecuredApiAction { implicit request =>
    dao.update(Suffix(suffix), from, to).flatMap { _ => noContent() }
  }

  def updateCustomFields(suffix: String, field: String, from: Option[Long], to: Option[Long]): Action[AnyContent] = Action {
    dao.updateCustomFields(Suffix(suffix), field, from, to)
    Ok("Updating")
  }
}