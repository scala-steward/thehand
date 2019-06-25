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

package scm

import dao._
import javax.inject.Inject
import models.Suffix
import org.tmatesoft.svn.core.SVNLogEntry
import tasks.TaskParser

import scala.concurrent.{ ExecutionContextExecutor, Future }
import play.api.db.slick.DatabaseConfigProvider
import tasks.{ ProcessTargetConnector, TaskConnector }

class SvnRepositoryData @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, repository: ScmConnector[SVNLogEntry], suffix: Suffix)(implicit taskConnector: TaskConnector, parser: TaskParser) {

  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val tp = ProcessTargetConnector(taskConnector)

  lazy val daoTasks = new TaskDAO(dbConfigProvider)
  lazy val daoCustomFields = new CustomFieldsDAO(dbConfigProvider)
  lazy val daoCommits = new CommitDAO(dbConfigProvider)
  lazy val daoAuthors = new AuthorDAO(dbConfigProvider)
  lazy val daoFiles = new EntryFileDAO(dbConfigProvider)
  lazy val daoCommitFiles = new CommitEntryFileDAO(dbConfigProvider)
  lazy val daoCommitTasks = new CommitTaskDAO(dbConfigProvider)

  private def calculateRangeLimit(lastId: Long): (Long, Long) = {
    val lastIdDB: Long = if (lastId < 1) 1 else lastId
    val lastIdSvn: Long = if (repository.latestId < 1) 1 else repository.latestId
    if (lastIdDB != lastIdSvn) (lastIdDB, lastIdSvn) else (1, 1)
  }

  def fixRange(range: (Long, Long)): (Long, Long) = {
    lazy val last = repository.latestId
    range match {
      case (from, to) if from < 1 && ((to < 1) || (to > last)) => (1, last)
      case (from, to) if from < 1 => (1, to)
      case (from, to) if to > last => (from, last)
      case (from, to) => (from, to)
    }
  }

  def updateRange(range: (Long, Long), steps: Long = 1000): Future[Seq[Int]] = {
    val r = fixRange(range)
    doStep(r._1, r._2, steps)
  }

  def doStep(from: Long, to: Long, step: Long): Future[Seq[Int]] = {
    if (((to - from) / step) <= 0) {
      updateInRange(from, to)
    } else {
      updateInRange(from, from + step)
      doStep(from + step, to, step)
    }
  }

  def updateAuto(): Future[Seq[Int]] = {
    daoCommits
      .actionLatestRevision(suffix)
      .flatMap(lastId => updateRange(calculateRangeLimit(lastId.getOrElse(1))))
  }

  def updateInRange(startId: Long, endId: Long): Future[Seq[Int]] = {
    lazy val extractor = new SvnExtractor(repository.log(startId, endId), parser)
    val insertAll: Future[Seq[Int]] =
      for {
        _ <- daoTasks.insert(extractor.extractTasks.flatMap(tp.process), suffix)
        _ <- daoCustomFields.insert(extractor.extractTasks.flatMap(tp.processCustomFields), suffix)
        _ <- daoAuthors.insert(extractor.extractAuthors, suffix)
        _ <- daoCommits.insert(extractor.extractCommits, suffix)
        _ <- daoFiles.insert(extractor.extractFiles, suffix)
        _ <- daoCommitTasks.insert(extractor.extractCommitsTasks, suffix)
        c <- daoCommitFiles.insert(extractor.extractCommitsFiles, suffix)
      } yield c
    insertAll
  }

  def updateCustomFields(startId: Long, endId: Long): Future[Seq[Int]] = {
    lazy val extractor = new SvnExtractor(repository.log(startId, endId), parser)
    val insertAll: Future[Seq[Int]] =
      for {
        c <- daoCustomFields.insert(extractor.extractTasks.flatMap(tp.processCustomFields), suffix)
      } yield c
    insertAll
  }
}