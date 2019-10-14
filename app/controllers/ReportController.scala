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

import api.ApiController
import javax.inject._
import dao._
import models.{DatabaseSuffix, Dump, QueryLocalDate}
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
    maybeSeq(dao.countCommitByCustomField(suffix, customField, from.fromTime, to.toTime))
  }

  def listCommitLocCustomField(suffix: DatabaseSuffix, customField: String, from: QueryLocalDate, to: QueryLocalDate): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.countCommitLocByCustomField(suffix, customField, from.fromTime, to.toTime))
  }

  private def getCompleteFilename(suffixName: String, filename: String) = {
    val localDirectory = new java.io.File(".").getCanonicalPath
    val reportDirectory = s"$localDirectory/report/"
    s"${reportDirectory}\\${suffixName.toLowerCase}${filename}"
  }

  // UNSAFE SECTION
  def listCommitsCustomFieldCsv(suffix: DatabaseSuffix, fieldValue: String, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    lazy val file = "_commits_bugs_counter";
    val writer: CvsIO.type = CvsIO
    lazy val filename = getCompleteFilename(suffix.suffix, file)
    dao.countCommitByCustomField(suffix, fieldValue, from.fromTime, to.toTime)
      .map {
      lines => writer.write(filename, lines)
      Ok.sendFile(new File(filename+".csv"), inline=true)
        .withHeaders(CACHE_CONTROL->"max-age=3600",CONTENT_DISPOSITION->s"attachment; filename=$file.csv", CONTENT_TYPE->"application/x-download");
    }
  }

  def listCommitsLocCustomFieldCsv(suffix: DatabaseSuffix, fieldValue: String, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    lazy val file = "_commits_bugs_loc_counter";
    lazy val writer: CvsIO.type = CvsIO
    lazy val filename = getCompleteFilename(suffix.suffix, file)
    dao.countCommitLocByCustomField(suffix, fieldValue, from.fromTime, to.toTime)
      .map {
      lines => writer.writeSLI(filename, lines)
      Ok.sendFile(new File(filename+".csv"), inline=true)
        .withHeaders(CACHE_CONTROL->"max-age=3600",CONTENT_DISPOSITION->s"attachment; filename=$file.csv", CONTENT_TYPE->"application/x-download");
    }
  }

  def dump(suffix: DatabaseSuffix, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    lazy val file = "_dump";
    lazy val writer: CvsIO.type = CvsIO
    lazy val filename = getCompleteFilename(suffix.suffix, file)
    dao.dump(suffix, from.fromTime, to.toTime)
      .map(_.map{r => Dump(r._1, r._2, r._3, r._4, r._5, r._6, r._7, r._8, r._9, r._10, r._11, r._12)})
      .map {
        lines => writer.writeDump(filename, lines)
          Ok.sendFile(new File(filename+".csv"), inline=true)
            .withHeaders(CACHE_CONTROL->"max-age=3600",CONTENT_DISPOSITION->s"attachment; filename=$file.csv", CONTENT_TYPE->"application/x-download");
      }
  }
}
