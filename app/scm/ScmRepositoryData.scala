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
import models.{DatabaseSuffix, FixedRange}
import tasks.TaskProcessConnector

import scala.concurrent.{ExecutionContextExecutor, Future}
import play.api.db.slick.DatabaseConfigProvider
import telemetrics.HandLogger

class ScmRepositoryData[T] @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider,
 repository: ScmConnector[T],
 extractor: ScmExtractor[T],
 taskProcessor: TaskProcessConnector,
 suffix: DatabaseSuffix)
{
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val daoTasks = new TaskDAO(dbConfigProvider)
  lazy val daoCustomFields = new CustomFieldsDAO(dbConfigProvider)
  lazy val daoCommits = new CommitDAO(dbConfigProvider)
  lazy val daoAuthors = new AuthorDAO(dbConfigProvider)
  lazy val daoFiles = new EntryFileDAO(dbConfigProvider)
  lazy val daoCommitFiles = new CommitEntryFileDAO(dbConfigProvider)
  lazy val daoCommitTasks = new CommitTaskDAO(dbConfigProvider)

  private def calculateRangeLimit(lastId: Long, latestId: Long): FixedRange = {
    val lastIdDB: Long = if (lastId < 1L) 1L else lastId
    val lastIdScm: Long = if (latestId < 1L) 1L else latestId
    if (lastIdDB != lastIdScm) FixedRange(lastIdDB, lastIdScm) else FixedRange(1L, 1L)
  }

  def fixRange(range: FixedRange): FixedRange = {
    val last = repository.latestId.getOrElse(-1L)
    range match {
      case r if r.begin < 1L && ((r.end < 1L) || (r.end > last)) => FixedRange(1L, last)
      case r if r.begin < 1L => FixedRange(1L, r.end)
      case r if r.end > last => FixedRange(r.begin, last)
      case r => FixedRange(r.begin, r.end)
    }
  }

  def updateRange(range: FixedRange, steps: Long = 1L): Future[Seq[Int]] = {  //low speed
    val fixedRange = fixRange(range)
    Future.sequence(Seq(
      doStep(fixedRange.begin, fixedRange.end, steps),
      updateTasks(repository.log(range.begin, range.end)))
    )
    Future(Seq())
  }

  def doStep(from: Long, to: Long, step: Long): Future[Seq[Int]] = {
    HandLogger.debug(s"step ${from}-${to}")
    if (((to - from) / step) <= 0L) {
      updateInSvm(repository.log(from, to)).flatMap( _ => Future(Seq()))
    } else {
      updateInSvm(repository.log(from, from + step)).flatMap( _ => doStep(from + step, to, step))
    }
  }

  def updateAuto(): Future[Seq[Int]] =
    repository.latestId match {
      case Some(last) => daoCommits
        .actionLatestRevision(suffix)
        .flatMap(lastId => updateRange(calculateRangeLimit(lastId.getOrElse(-1), last)))
      case None => Future(Seq())
    }

  def updateTasks(data: Seq[T]): Future[Seq[Int]] = {
   val twc = taskProcessor.processRange(extractor.extractTasks(data), "Request Type")
   for {
      _ <- daoTasks.insert(twc.map(_.task), suffix)
      c <- daoCustomFields.insert(twc.flatMap(_.custom), suffix)
   } yield c
  }

  def updateInSvm(data: Seq[T]): Future[Seq[Int]] =
    for {
      _ <- daoAuthors.insert(extractor.extractAuthors(data), suffix)
      _ <- daoCommits.insert(extractor.extractCommits(data), suffix)
      _ <- daoFiles.insert(extractor.extractFiles(data), suffix)
      _ <- daoCommitTasks.insert(extractor.extractCommitsTasks(data), suffix)
      c <- daoCommitFiles.insert(extractor.extractCommitsFiles(data), suffix)
    } yield c

  def updateCustomFields(field: String, startId: Long, endId: Long): Future[Seq[Int]] = {
    lazy val data = repository.log(startId, endId)
    val insertAll: Future[Seq[Int]] =
      for {
        c <- daoCustomFields.insert(extractor.extractTasks(data).flatMap(taskProcessor.processCustomFields(_, field)), suffix)
      } yield c
    insertAll
  }
}