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

package cross

import dao._
import javax.inject.Inject
import models.Suffix
import org.tmatesoft.svn.core.SVNLogEntry
import thehand.TaskParser
import thehand.tasks.{ProcessTargetConnector, TaskConnector}
import thehand.telemetrics.HandLogger

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import play.api.db.slick.DatabaseConfigProvider

class SvnRepositoryData @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, taskConnector: TaskConnector, repository: ScmConnector[SVNLogEntry], parser: TaskParser) {
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val tp = ProcessTargetConnector(taskConnector)

  implicit val suffix: Suffix = Suffix("eb_")

  def updateInStep(begin: Long, end: Long, step: Long): Unit = {
    if (((end - begin) / step) <= 0) updateRange(begin, end)
    else {
      updateRange(begin, begin + step)
      updateInStep(begin + step, end, step)
    }
  }



  def updateAuto(): Unit = {
    val dao = new CommitDAO(dbConfigProvider)
    val lastIdDB = if (dao.latestId < 1) 1 else dao.latestId
    val lastIdSvn = if (repository.latestId < 1) 1 else repository.latestId
    if (lastIdDB != lastIdSvn) {
      HandLogger.info("Start at revision #" + lastIdDB + " until #" + lastIdSvn)
      updateInStep(lastIdDB, lastIdSvn, 5000)
    }
  }

  def runRaise[T](f: Future[T]): Unit = f onComplete {
    case Success(_) => HandLogger.debug("correct write tasks")
    case Failure(e) => HandLogger.error("error in writing tasks " + e.getMessage)
  }

  def updateRange(startId: Long, endId: Long): Unit = {
    val extractor = new SvnExtractor(repository.log(startId, endId), parser)

    val daoT = new TaskDAO(dbConfigProvider)
    daoT.insert(extractor.extractTasks.flatMap(tp.process)) onComplete {
      case Success(_) => HandLogger.debug("correct write tasks")
      case Failure(e) => HandLogger.error("error in writing tasks " + e.getMessage)
    }

    val daoA = new AuthorDAO(dbConfigProvider)
    daoA.insert(extractor.extractAuthors) onComplete {
      case Success(_) => HandLogger.debug("correct write authors")
      case Failure(e) => HandLogger.error("error in writing authors " + e.getMessage)
    }

    val daoC = new CommitDAO(dbConfigProvider)
    daoC.insert(extractor.extractCommits) onComplete {
      case Success(_) => HandLogger.debug("correct create commits")
      case Failure(e) => HandLogger.error("error in writing commits " + e.getMessage)
    }

    val daoF = new EntryFileDAO(dbConfigProvider)
    daoF.insert(extractor.extractFiles) onComplete {
      case Success(_) => HandLogger.debug("correct create files")
      case Failure(e) => HandLogger.error("error in writing files " + e.getMessage)
    }

    val daoCt = new CommitTaskDAO(dbConfigProvider)
    daoCt.insert(extractor.extractCommitsTasks) onComplete {
      case Success(_) => HandLogger.debug("correct create commits tasks")
      case Failure(e) => HandLogger.error("error in writing commits tasks " + e.getMessage)
    }

    val daoCf = new CommitEntryFileDAO(dbConfigProvider)
    daoCf.insert(extractor.extractCommitsFiles) onComplete {
      case Success(_) => HandLogger.debug("correct create commits files")
      case Failure(e) => HandLogger.error("error in writing commits files " + e.getMessage)
    }
  }
}