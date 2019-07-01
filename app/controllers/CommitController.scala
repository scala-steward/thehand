/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers

import java.sql.Timestamp
import java.time.LocalTime

import api.ApiController
import javax.inject._
import dao.CommitDAO
import models.{DatabaseSuffix, QueryLocalDate}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.mvc._

import scala.concurrent.ExecutionContext

class CommitController @Inject()
(override val dbc: DatabaseConfigProvider, dao: CommitDAO, l: Langs, mcc: MessagesControllerComponents)
(implicit ec: ExecutionContext)
  extends ApiController(dbc, l, mcc)  {

  def list(suffix: DatabaseSuffix): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.list(suffix))
  }

  def info(suffix: DatabaseSuffix, id: Long): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.info(suffix, id))
  }

  def infoRevision(suffix: DatabaseSuffix, revision: Long): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.infoRevision(suffix, revision))
  }

  def infoDate(suffix: DatabaseSuffix, from: QueryLocalDate, to: QueryLocalDate): Action[Unit] = ApiAction { implicit request =>
    val fromTime = Timestamp.valueOf(from.date.atTime(LocalTime.MIDNIGHT))
    val toTime = Timestamp.valueOf(to.date.atTime(LocalTime.MIDNIGHT))
    maybeSeq(dao.infoDate(suffix, fromTime, toTime))
  }
}