/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers

import javax.inject._
import dao._
import models.Suffix
import play.api.libs.json.Json
import play.api.mvc._
import thehand.report.CvsIO
import thehand.telemetrics.HandLogger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class ReportController @Inject()(dao: ReportDao,
                                 cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val repositoryName = "AHHAHAHA"
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

  private def exec[T](f: Future[T]) = {
    val result = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def reportFilesBugsCounter(suffix: Suffix): Unit = {
    lazy val filename = s"./reports/report_bugs_${repositoryName.toLowerCase}_counter"
    reportFilesBugCounter(suffix) onComplete {
      case scala.util.Success(value) =>
        writer.write(filename, value.sortBy(_._2))
        HandLogger.info("generate " + repositoryName + " files bugs report")
      case scala.util.Failure(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def authorReport(authorName: String, suffix: Suffix): Unit = {
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${repositoryName.toLowerCase}_commits_counter"
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
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${repositoryName.toLowerCase}_commits_bugs_counter"
    exec[Seq[(String, Int)]](reportAuthorBugsCommitsCounter(authorName, suffix)) match {
      case Right(values) =>
        writer.write(filename, values.sortBy(_._2))
        HandLogger.info("generate author bugs report " + authorName)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsBugsReports(suffix: Suffix): Unit = {
    exec[Seq[String]](authors(suffix)) match {
      case Right(values) => values.foreach(authorBugsReport(_,suffix))
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  // ActionsFunciton ###################################################################

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
}
