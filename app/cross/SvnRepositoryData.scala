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
import play.api.db.slick.DatabaseConfigProvider

class SvnRepositoryData @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, taskConnector: TaskConnector, repository: ScmConnector[SVNLogEntry], parser: TaskParser, suffix: Suffix) {
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val tp = ProcessTargetConnector(taskConnector)

  def updateInStep(from: Long, to: Long, step: Long): Unit = {
    val lessThenStep = ((to - from) / step) <= 0
    if (lessThenStep) updateRange(from, to)
    else {
      updateRange(from, from + step)
      updateInStep(from + step, to, step)
    }
  }

  private def updatePrimitive(o : Option[Long]) = {
    val lastId: Long = o.getOrElse(1)
    val lastIdDB: Long = if (lastId < 1) 1 else lastId
    val lastIdSvn: Long = if (repository.latestId < 1) 1 else repository.latestId
    if (lastIdDB != lastIdSvn) {
      HandLogger.info("Start at revision #" + lastIdDB + " until #" + lastIdSvn)
      updateInStep(lastIdDB, lastIdSvn, 1000)
    }
  }

  def updateAuto() = {
    val dao = new CommitDAO(dbConfigProvider)
    dao.actionLatestRevision(suffix).foreach(updatePrimitive)
  }

  def updateRange(startId: Long, endId: Long): Future[Seq[Int]] = {
    HandLogger.info("updating" + startId + " to " + endId)
    val extractor = new SvnExtractor(repository.log(startId, endId), parser)

    lazy val daoT = new TaskDAO(dbConfigProvider)
    lazy val daoA = new AuthorDAO(dbConfigProvider)
    lazy val daoC = new CommitDAO(dbConfigProvider)
    lazy val daoF = new EntryFileDAO(dbConfigProvider)
    lazy val daoCt = new CommitTaskDAO(dbConfigProvider)
    lazy val daoCf = new CommitEntryFileDAO(dbConfigProvider)

    lazy val run: Seq[Future[Seq[Int]]] =
      Seq(daoT.insert(extractor.extractTasks.flatMap(tp.process), suffix),
      daoA.insert(extractor.extractAuthors, suffix),
      daoC.insert(extractor.extractCommits, suffix),
      daoC.insert(extractor.extractCommits, suffix),
      daoF.insert(extractor.extractFiles, suffix),
      daoCt.insert(extractor.extractCommitsTasks, suffix),
      daoCf.insert(extractor.extractCommitsFiles, suffix)
    )

    Future.sequence(run).map(_.flatten)
  }
}