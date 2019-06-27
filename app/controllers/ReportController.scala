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
import java.text.SimpleDateFormat

import javax.inject._
import dao._
import models.Suffix
import play.api.libs.json.Json
import play.api.mvc._
import reportio.CvsIO
import telemetrics.HandLogger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ReportController @Inject() (
  dao: ReportDAO,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  implicit val writer: CvsIO.type = CvsIO

  private def authors(suffix: Suffix): Future[Seq[String]] = {
    dao.authorsNames(suffix)
  }

  private def reportFilesBugCounter(suffix: Suffix): Future[Seq[(String, Int)]] = {
    dao.filesBugsCounter(suffix)
  }

  private def reportAuthorCommitsCounter(authorName: String, suffix: Suffix): Future[Seq[(String, Int)]] = {
    dao.fileAuthorCommitsCounter(authorName, suffix)
  }

  private def reportAuthorBugsCommitsCounter(authorName: String, suffix: Suffix): Future[Seq[(String, Int)]] = {
    dao.fileAuthorCommitsBugsCounter(authorName, suffix)
  }

  private def reportCommitByCustomField(suffix: Suffix, fieldValue: String, initialTime: Timestamp, finalTime: Timestamp): Future[Seq[(String, Int)]] = {
    dao.countCommitByCustomField(suffix, fieldValue, initialTime, finalTime)
  }

  private def exec[T](f: Future[T]) = {
    val result = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def reportFilesBugsCounter(suffix: Suffix): Unit = {
    lazy val filename = s"./reports/report_bugs_${suffix.suffix.toLowerCase}_counter"
    reportFilesBugCounter(suffix) onComplete {
      case scala.util.Success(value) =>
        writer.write(filename, value.sortBy(_._2))
        HandLogger.info("generate " + suffix.suffix + " files bugs report")
      case scala.util.Failure(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def authorReport(authorName: String, suffix: Suffix): Unit = {
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${suffix.suffix.toLowerCase}_commits_counter"
    exec[Seq[(String, Int)]](reportAuthorCommitsCounter(authorName, suffix)) match {
      case Right(values) =>
        writer.write(filename, values.sortBy(_._2))
        HandLogger.info("generate author report " + authorName)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsReports(suffix: Suffix): Unit = {
    exec[Seq[String]](authors(suffix)) match {
      case Right(values) => values.foreach(authorReport(_, suffix))
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def authorBugsReport(authorName: String, suffix: Suffix): Unit = {
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${suffix.suffix.toLowerCase}_commits_bugs_counter"
    exec[Seq[(String, Int)]](reportAuthorBugsCommitsCounter(authorName, suffix)) match {
      case Right(values) =>
        writer.write(filename, values.sortBy(_._2))
        HandLogger.info("generate author bugs report " + authorName)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsBugsReports(suffix: Suffix): Unit = {
    exec[Seq[String]](authors(suffix)) match {
      case Right(values) => values.foreach(authorBugsReport(_, suffix))
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  // ActionsFunctions ##################################################################

  private def reportFilesBugsCounterToAction(suffix: Suffix) = {
    reportFilesBugCounter(suffix)
  }

  private def authorBugsReportToAction(authorName: String, suffix: Suffix) = {
    reportAuthorBugsCommitsCounter(authorName, suffix)
  }

  // Actions ###########################################################################

  def getAuthors(suffix: String): Action[AnyContent] = Action.async {
    val s = Suffix(suffix)
    authors(s).map { a =>
      Ok(Json.toJson(a))
    }
  }

  def getFilesBugs(suffix: String): Action[AnyContent] = Action.async {
    val s = Suffix(suffix)
    reportFilesBugsCounterToAction(s).map { a =>
      Ok(Json.toJson(a))
    }
  }

  def getAuthorBugs(author: String, suffix: String): Action[AnyContent] = Action.async {
    val s = Suffix(suffix)
    authorBugsReportToAction(author, s).map { a =>
      Ok(Json.toJson(a))
    }
  }

  private def parseTimestamp(fromTime: String, toTime: String) : Try[(Timestamp, Timestamp)] = Try {
      val format = new SimpleDateFormat("yyyy-MM-dd")
      (new Timestamp(format.parse(fromTime).getTime), new Timestamp(format.parse(toTime).getTime))
    }


  def getCommitByCustomField(suffix: String, customField: String, fromTime: String, toTime: String): Action[AnyContent] = Action.async {
    parseTimestamp(fromTime, toTime) match {
      case Success(times) =>
        reportCommitByCustomField(Suffix(suffix), customField, times._1, times._2).map { a => Ok(Json.toJson(a.sortBy(i => -i._2))) }
      case Failure(_) => Future(BadRequest("valid date format => yyyy-MM-dd"))
    }
  }

  def getCommitByCustomFieldCsv(suffix: String, fieldValue: String, fromTime: String, toTime: String): Action[AnyContent] = Action.async {
    parseTimestamp(fromTime, toTime) match {
      case Success(times) =>
        // hiro fix path and correct parametrize in other function
        val localDirectory = new java.io.File(".").getCanonicalPath
        val reportDirectory = s"${localDirectory}/report/"
        val file = s"${suffix.toLowerCase}_commits_bugs_counter"
        lazy val filename = s"${reportDirectory}${file}"
        reportCommitByCustomField(Suffix(suffix), fieldValue, times._1, times._2).map { a =>
          writer.write(filename, a.sortBy(i => i._2))

          Ok.sendFile(new File(filename+".csv"), inline=true)
            .withHeaders(CACHE_CONTROL->"max-age=3600",CONTENT_DISPOSITION->s"attachment; filename=${file}.csv", CONTENT_TYPE->"application/x-download");
        }
      case Failure(_) => Future(BadRequest("valid date format => yyyy-MM-dd"))
    }
  }
}
