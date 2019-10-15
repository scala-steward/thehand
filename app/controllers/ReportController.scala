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
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.i18n.Langs
import play.api.mvc._
import reportio.{CvsIO, Writable}

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

  def dump(suffix: DatabaseSuffix, from: QueryLocalDate, to: QueryLocalDate): Action[Unit] = ApiAction { implicit request =>
    maybeSeq(dao.dump(suffix, from.toTime, to.toTime))
  }

  private def generateCsvFile(rows: Seq[Writable]) =
      Ok(CvsIO.write(rows)).withHeaders("Content-Type" -> "text/csv",
        "Content-Disposition" -> "attachment; filename=report.csv")

  def listCommitsCustomFieldCsv(suffix: DatabaseSuffix, fieldValue: String, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    dao.countCommitByCustomField(suffix, fieldValue, from.fromTime, to.toTime)
      .map(_.map{r => DumpCounter(r._1, r._2)}.sorted.reverse)
      .map(generateCsvFile)
  }

  def listCommitsLocCustomFieldCsv(suffix: DatabaseSuffix, fieldValue: String, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    dao.countCommitLocByCustomField(suffix, fieldValue, from.fromTime, to.toTime)
      .map(_.map{r => DumpLocCounter(r._1, r._2, r._3)}.sorted.reverse)
      .map(generateCsvFile)
  }

  def dumpCsv(suffix: DatabaseSuffix, from: QueryLocalDate, to: QueryLocalDate): Action[AnyContent] = Action.async {
    dao.dump(suffix, from.fromTime, to.toTime)
      .map(_.map{r => DumpJoinDatabase(r._1, r._2, r._3, r._4, r._5, r._6, r._7, r._8, r._9, r._10, r._11, r._12)})
      .map(generateCsvFile)
  }
}
