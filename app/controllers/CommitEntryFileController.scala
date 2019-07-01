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
import models.DatabaseSuffix
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.mvc._

import scala.concurrent.ExecutionContext

class CommitEntryFileController @Inject()
(override val dbc: DatabaseConfigProvider, dao: CommitEntryFileDAO, l: Langs, mcc: MessagesControllerComponents)
(implicit ec: ExecutionContext)
  extends ApiController(dbc, l, mcc)  {

  def list(suffix: DatabaseSuffix): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.list(suffix))
  }

  def info(suffix: DatabaseSuffix, id: Long): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.info(suffix, id))
  }
}