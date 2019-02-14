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

package thehand.schemas

import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import telemetrics.HandLogger

import scala.util.{Failure, Success}

class RepositoryDaoSpec extends AsyncFlatSpec with Matchers {
  implicit val context = ExecutionContext.Implicits.global
  lazy val dao: RepositoryDao = new RepositoryDao(slick.jdbc.H2Profile, "testConfig", "test_")

  lazy val extractor = new ExtractorFixture()
  dao.writeTasks(extractor.extractTasks) onComplete {
    case Success(_) => HandLogger.debug("correct write tasks")
    case Failure(e) => HandLogger.error("error in writing tasks " + e.getMessage)
  }

  dao.writeAuthors(extractor.extractAuthors) onComplete {
    case Success(_) => HandLogger.debug("correct write authors")
    case Failure(e) => HandLogger.error("error in writing authors " + e.getMessage)
  }

  dao.writeCommits(extractor.extractCommits) onComplete {
    case Success(_) => HandLogger.debug("correct create commits")
    case Failure(e) => HandLogger.error("error in writing commits " + e.getMessage)
  }

  dao.writeFiles(extractor.extractFiles) onComplete {
    case Success(_) => HandLogger.debug("correct create files")
    case Failure(e) => HandLogger.error("error in writing files " + e.getMessage)
  }

  dao.writeCommitsFiles(extractor.extractCommitsFiles) onComplete {
    case Success(_) => HandLogger.debug("correct create commits files")
    case Failure(e) => HandLogger.error("error in writing commits files " + e.getMessage)
  }

  dao.writeCommitsTasks(extractor.extractCommitsTasks) onComplete {
    case Success(_) => HandLogger.debug("correct create commits tasks")
    case Failure(e) => HandLogger.error("error in writing commits tasks " + e.getMessage)
  }

  "Table Tasks" should "have five tasks" in {
    dao.countTasks map { counter => assert(counter == 5) }
  }

  "Table Authors" should "have three authors" in {
    dao.countAuthors map { counter => assert(counter == 3) }
  }

  "Table Commits" should "have three commits" in {
    dao.countCommits map { counter => assert(counter == 3) }
  }

  "Table Files" should "have three files" in {
    dao.countFiles map { counter => assert(counter == 3) }
  }

  "Table Commits Files" should "have eight commits files" in {
    dao.countCommitsFiles map { counter => assert(counter == 6) }
  }

  "Table Commits Tasks" should "have three commits tasks" in {
    dao.countCommitTasks map { counter => assert(counter == 4) }
  }

  "LastestCommit revision" should "be three" in {
    dao.actionLatestRevision map { counter => assert(counter.getOrElse(-1) == 3) }
  }
}
