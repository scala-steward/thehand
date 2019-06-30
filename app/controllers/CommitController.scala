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

import javax.inject._
import dao._
import models.{QueryLocalDate, DatabeSuffix}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class CommitController @Inject() (
  dao: CommitDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def list(suffix: DatabeSuffix): Action[AnyContent] = Action.async {
    dao.list(suffix).map { a =>
      Ok(Json.toJson(a))
    }
  }

  def info(suffix: DatabeSuffix, id: Long): Action[AnyContent] = Action.async {
    dao.info(suffix, id).map { a =>
      Ok(Json.toJson(a))
    }
  }

  def infoRevision(suffix: DatabeSuffix, revision: Long): Action[AnyContent] = Action.async {
    dao.infoRevision(suffix, revision).map { a =>
      Ok(Json.toJson(a))
    }
  }

  def infoDate(suffix: DatabeSuffix, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    val fromTime = Timestamp.valueOf(from.date.atTime(LocalTime.MIDNIGHT))
    val toTime = Timestamp.valueOf(to.date.atTime(LocalTime.MIDNIGHT))
    dao.infoDate(suffix, fromTime, toTime).map { a =>
      Ok(Json.toJson(a))
    }
  }
}