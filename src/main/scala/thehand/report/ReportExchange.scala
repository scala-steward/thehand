/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package thehand.report

import telemetrics.HandLogger
import thehand.schemas.ReportsDao

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class ReportExchange (dao: ReportsDao, repositoryName: String)(implicit writer: ReportIO) {
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)

  def authors: Future[Seq[String]] = {
    dao.authorsNames
  }

  def reportFilesBugCounter: Future[Seq[(String, Int)]] = {
    dao.filesBugsCounter
  }

  def reportAuthorCommitsCounter(authorName: String): Future[Seq[(String, Int)]] = {
    dao.fileAuthorCommitsCounter(authorName)
  }

  def reportAuthorBugsCommitsCounter(authorName: String): Future[Seq[(String, Int)]] = {
    dao.fileAuthorCommitsBugsCounter(authorName)
  }

  private def exec[T](f: Future[T]) = {
    val result = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither
  }

  def close() = dao.close

  def reportFilesBugsCounter() = {
    lazy val filename = s"./reports/report_bugs_${repositoryName.toLowerCase}_counter"
    reportFilesBugCounter onComplete {
      case scala.util.Success(value) =>
        writer.write(filename, value.sortBy(_._2))
        HandLogger.info("generate " + repositoryName + " files bugs report")
      case scala.util.Failure(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def authorReport(authorName: String) = {
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${repositoryName.toLowerCase}_commits_counter"
    exec[Seq[(String, Int)]](reportAuthorCommitsCounter(authorName)) match {
      case Right(values) =>
        writer.write(filename, values.sortBy(_._2))
        HandLogger.info("generate author report " + authorName)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsReports()= {
    exec[Seq[String]](authors) match {
      case Right(values) => values.map(authorReport)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def authorBugsReport(authorName: String) = {
    lazy val filename = s"./reports/author_${authorName.toLowerCase()}_${repositoryName.toLowerCase}_commits_bugs_counter"
    exec[Seq[(String, Int)]](reportAuthorBugsCommitsCounter(authorName)) match {
      case Right(values) =>
        writer.write(filename, values.sortBy(_._2))
        HandLogger.info("generate author bugs report " + authorName)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsBugsReports()= {
    exec[Seq[String]](authors) match {
      case Right(values) => values.map(authorBugsReport)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }
}
