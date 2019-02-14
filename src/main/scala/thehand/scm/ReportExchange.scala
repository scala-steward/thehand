/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package thehand.scm

import thehand.schemas.ReportsDao

import scala.concurrent.{ExecutionContextExecutor, Future}

class ReportExchange (dao: ReportsDao) {
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)

  def authors: Future[Seq[(String)]] = {
    dao.authorsNames
  }

  def reportFilesBugCounter: Future[Seq[(String, Int)]] = {
    dao.filesBugsCounter
  }

  def reportAuthorCommitsCounter(autorName: String): Future[Seq[(String, Int)]] = {
    dao.fileAuthorCommitsCounter(autorName)
  }

  def close() = dao.close
}
