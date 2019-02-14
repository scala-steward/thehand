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

import thehand.schemas._
import org.tmatesoft.svn.core.SVNLogEntry
import telemetrics.HandLogger
import thehand.TaskParser
import thehand.tasks.{ProcessTargetConnector, TaskConnector}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class SvnRepositoryData(dao: RepositoryDao, taskConnector: TaskConnector, repository: ScmConnector[SVNLogEntry], parser: TaskParser) {
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val tp = ProcessTargetConnector(taskConnector)

  def close() = dao.close

  def updateInStep(begin: Long, end: Long, step: Long): Unit = {
    if (((end - begin) / step) <= 0) updateRange(begin, end)
    else {
      updateRange(begin, begin + step)
      updateInStep(begin + step, end, step)
    }
  }

  def updateAuto(): Unit = {
    val lastIdDB = if (dao.latestId < 1) 1 else dao.latestId
    val lastIdSvn = if (repository.latestId < 1) 1 else repository.latestId
    if (lastIdDB != lastIdSvn) {
      HandLogger.info("Start at revision #" + lastIdDB + " until #" + lastIdSvn)
      updateInStep(lastIdDB, lastIdSvn, 5000)
    }
  }

  def runRaise[T](f: Future[T]) = f onComplete {
    case Success(_) => HandLogger.debug("correct write tasks")
    case Failure(e) => dao.close
    HandLogger.error("error in writing tasks " + e.getMessage)
  }

  def updateRange(startId: Long, endId: Long): Unit = {
    val extractor = new SvnExtractor(repository.log(startId, endId), parser)

    dao.writeTasks(extractor.extractTasks.flatMap(tp.process)) onComplete {
      case Success(_) => HandLogger.debug("correct write tasks")
      case Failure(e) => dao.close
        HandLogger.error("error in writing tasks " + e.getMessage)
    }

    dao.writeAuthors(extractor.extractAuthors) onComplete {
      case Success(_) => HandLogger.debug("correct write authors")
      case Failure(e) => dao.close
        HandLogger.error("error in writing authors " + e.getMessage)
    }

    dao.writeCommits(extractor.extractCommits) onComplete {
      case Success(_) => HandLogger.debug("correct create commits")
      case Failure(e) => dao.close
        HandLogger.error("error in writing commits " + e.getMessage)
    }

    dao.writeFiles(extractor.extractFiles) onComplete {
      case Success(_) => HandLogger.debug("correct create files")
      case Failure(e) => dao.close
        HandLogger.error("error in writing files " + e.getMessage)
    }

    dao.writeCommitsTasks(extractor.extractCommitsTasks) onComplete {
      case Success(_) => HandLogger.debug("correct create commits tasks")
      case Failure(e) => dao.close
        HandLogger.error("error in writing commits tasks " + e.getMessage)
    }

    dao.writeCommitsFiles(extractor.extractCommitsFiles) onComplete {
      case Success(_) => HandLogger.debug("correct create commits files")
      case Failure(e) => dao.close
        HandLogger.error("error in writing commits files " + e.getMessage)
    }
  }
}