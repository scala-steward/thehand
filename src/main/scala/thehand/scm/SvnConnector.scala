/*
 * Copyright (c) 2018, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 *
 *
 */

package thehand.scm

import org.tmatesoft.svn.core.SVNLogEntry
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl
import org.tmatesoft.svn.core.io.SVNRepository
import telemetrics.HandLogger

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object SvnConnector {
  DAVRepositoryFactory.setup()
  SVNRepositoryFactoryImpl.setup()
  FSRepositoryFactory.setup()
}

class SvnConnector(repository: SVNRepository) extends ScmConnector[SVNLogEntry] {
  private def tryLog(startRev: Long, endRev: Long): Try[Seq[SVNLogEntry]] = Try {
    repository
      .log(Array[String](""), null, startRev, endRev, true, true)
      .asScala
      .map(_.asInstanceOf[SVNLogEntry])
      .toSeq
  }

  private def logMap(startRev: Long, endRev: Long): Seq[SVNLogEntry] =
    tryLog(startRev, endRev) match {
      case Success(i) => i
      case Failure(e) =>
        HandLogger.error("error while collecting log information " + e.getMessage)
        Nil
    }

  def log(startRev: Long, endRev: Long): Seq[SVNLogEntry] = {
    logMap(startRev, endRev)
  }

  def show(startRev: Long, endRev: Long, showFunction: SVNLogEntry => Unit): Unit = {
    logMap(startRev, endRev)
      .foreach(showFunction)
  }

  private def getLatestRevision: Try[Long] = Try {
    repository.getLatestRevision
  }

  def latestId: Long = getLatestRevision match {
    case Success(rev) => rev
    case Failure(e) =>
      HandLogger.error("error while fetching the latest repository revision: " + e.getMessage)
      -1
  }
}
