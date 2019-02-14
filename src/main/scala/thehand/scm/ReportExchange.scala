/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package thehand.scm

import java.io.File

import thehand.schemas.ReportsDao
import com.github.tototoshi.csv._
import telemetrics.HandLogger

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ReportExchange {
  def apply(dao: ReportsDao, name: String): ReportExchange = new ReportExchange(dao, name)
}

class ReportExchange (dao: ReportsDao, repositoryName: String) {
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

  def close() = dao.close

  private def writeCvsReport(filename: String, lines: Seq[(String, Int)]) = {
    val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    lines.reverse.map(t => List(t._2, t._1)).map(writer.writeRow(_))
    writer.close()
  }

  def reportFilesBugsCounterCvs() = {
    reportFilesBugCounter onComplete {
      case scala.util.Success(value) => writeCvsReport("./reports/" + repositoryName + "report_files_bugs_counter", value.sortBy(_._2))
      case scala.util.Failure(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  private def autorReport(authorName: String) = {
    HandLogger.info("generating author report " + authorName)
    reportAuthorCommitsCounter(authorName) onComplete {
      case scala.util.Success(value) =>
        writeCvsReport("./reports/"+repositoryName+"_"+authorName+"_author_commits_counter", value.sortBy(_._2))
      case scala.util.Failure(e) => HandLogger.error("error" + e.getMessage)
    }
  }

  def authorsReports()= {
    val f = authors
    val result = Await.ready(f, Duration.Inf).value.get
    val resultEither = result match {
      case Success(t) => Right(t)
      case Failure(e) => Left(e)
    }
    resultEither match {
      case Right(values) => values.map(autorReport)
      case Left(e) => HandLogger.error("error" + e.getMessage)
    }
  }
}
