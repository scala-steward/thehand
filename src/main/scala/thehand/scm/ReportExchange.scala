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

import scala.concurrent.{ExecutionContextExecutor, Future}

object ReportExchange {
  def apply(dao: ReportsDao): ReportExchange = new ReportExchange(dao)
}

class ReportExchange (dao: ReportsDao) {
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

  def writeReport(filename: String, lines: Seq[(String, Int)]) = {
    val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    lines.reverse.map(t => List(t._2, t._1)).map(writer.writeRow(_))
    writer.close()
  }
}
