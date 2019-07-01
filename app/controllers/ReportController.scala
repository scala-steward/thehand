/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers

import java.io.File
import java.sql.Timestamp
import java.time.LocalTime

import api.ApiController
import javax.inject._
import dao._
import models.{DatabaseSuffix, QueryLocalDate}
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.mvc._
import reportio.CvsIO

import scala.concurrent.ExecutionContext

class ReportController @Inject()
(override val dbc: DatabaseConfigProvider, dao: ReportDAO, l: Langs, mcc: MessagesControllerComponents)
(implicit ec: ExecutionContext)
  extends ApiController(dbc, l, mcc)  {

  def getFilesBugs(suffix: DatabaseSuffix): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.filesBugsCounter(suffix))
  }

  def getAuthorBugs(author: String, suffix: DatabaseSuffix): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.fileAuthorCommitsBugsCounter(author, suffix))
  }

  def listCommitCustomField(suffix: DatabaseSuffix, customField: String, from: QueryLocalDate, to: QueryLocalDate): Action[Unit] = ApiAction { implicit request =>
    val fromTime = Timestamp.valueOf(from.date.atTime(LocalTime.MIDNIGHT))
    val toTime = Timestamp.valueOf(to.date.atTime(LocalTime.MIDNIGHT))
    maybeSeq(dao.countCommitByCustomField(suffix, customField, fromTime, toTime))
  }

  // UNSAFE SECTION
  def listCommitsCustomFieldCsv(suffix: DatabaseSuffix, fieldValue: String, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    // hiro fix path and correct parametrize in other function
    val fromTime = Timestamp.valueOf(from.date.atTime(LocalTime.MIDNIGHT))
    val toTime = Timestamp.valueOf(to.date.atTime(LocalTime.MIDNIGHT))

    val localDirectory = new java.io.File(".").getCanonicalPath
    val reportDirectory = s"${localDirectory}/report/"
    val file = s"${suffix.suffix.toLowerCase}_commits_bugs_counter"
    lazy val filename = s"${reportDirectory}${file}"

    val writer: CvsIO.type = CvsIO
    dao.countCommitByCustomField(suffix, fieldValue, fromTime, toTime).map { a =>
      writer.write(filename, a.sortBy(i => i._2))
      Ok.sendFile(new File(filename+".csv"), inline=true)
        .withHeaders(CACHE_CONTROL->"max-age=3600",CONTENT_DISPOSITION->s"attachment; filename=${file}.csv", CONTENT_TYPE->"application/x-download");
    }
  }
}
