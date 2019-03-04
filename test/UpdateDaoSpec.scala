///*
// * Copyright (c) 2018, Jeison Cardoso. All Rights Reserved
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
// * the Free Software Foundation; either version 3, or (at your option)
// * any later version.
// *
// *
// */
//
//import dao._
//import javax.inject.Inject
//import models.Suffix
//import org.specs2.Specification
//import play.api.db.slick.DatabaseConfigProvider
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.test.{PlaySpecification, WithApplication}
//import telemetrics.HandLogger
//
//import scala.concurrent.{ExecutionContext, Future}
//import scala.util.{Failasure, Success}
//import play.db.NamedDatabase
//
//class UpdateDaoSpec extends PlaySpecification {
//
//  implicit val context = ExecutionContext.Implicits.global
//
//  Guice..createInjector()
//
//  val extractor = new ExtractorFixture()
//  val s = Suffix("echo")
//
//  val daoTasks = new TaskDAO(dbConfigProvider)
//  daoTasks.insert(extractor.extractTasks, s) onComplete {
//    case Success(_) => HandLogger.debug("correct write tasks")
//    case Failure(e) => HandLogger.error("error in writing tasks " + e.getMessage)
//  }
//
//  val daoAuthor = new AuthorDAO(dbConfigProvider)
//  daoAuthor.insert(extractor.extractAuthors, s) onComplete {
//    case Success(_) => HandLogger.debug("correct write authors")
//    case Failure(e) => HandLogger.error("error in writing authors " + e.getMessage)
//  }
//
//  val daoCommits = new CommitDAO(dbConfigProvider)
//  daoCommits.insert(extractor.extractCommits, s) onComplete {
//    case Success(_) => HandLogger.debug("correct create commits")
//    case Failure(e) => HandLogger.error("error in writing commits " + e.getMessage)
//  }
//
//  val daoFiles = new EntryFileDAO(dbConfigProvider)
//  daoFiles.insert(extractor.extractFiles, s) onComplete {
//    case Success(_) => HandLogger.debug("correct create files")
//    case Failure(e) => HandLogger.error("error in writing files " + e.getMessage)
//  }
//
//  val daoCommitFiles = new CommitEntryFileDAO(dbConfigProvider)
//  daoCommitFiles.insert(extractor.extractCommitsFiles, s) onComplete {
//    case Success(_) => HandLogger.debug("correct create commits files")
//    case Failure(e) => HandLogger.error("error in writing commits files " + e.getMessage)
//  }
//
//  val daoCommitTasks = new CommitTaskDAO(dbConfigProvider)
//  daoCommitTasks.insert(extractor.extractCommitsTasks, s) onComplete {
//    case Success(_) => HandLogger.debug("correct create commits tasks")
//    case Failure(e) => HandLogger.error("error in writing commits tasks " + e.getMessage)
//  }
//
//  it should "Table Tasks should have five tasks\"" in {
//    val futureCount: Future[Int] = daoTasks.countTasks(s)
//    futureCount map { counter => assert(counter == 5) }
//  }
//
//  "Table Tasks" should "have five tasks" in {
//    daoTasks.countTasks(s) map { counter => assert(counter == 5) }
//  }
//
//  "Table Authors" should "have three authors" in {
//    daoAuthor.countAuthors(s) map { counter => assert(counter == 3) }
//  }
//
//  "Table Commits" should "have three commits" in {
//    daoCommits.countCommits(s) map { counter => assert(counter == 3) }
//  }
//
//  "Table Files" should "have three files" in {
//    daoFiles.countFiles(s) map { counter => assert(counter == 3) }
//  }
//
//  "Table Commits Files" should "have eight commits files" in {
//    daoCommitFiles.countCommitsFiles(s) map { counter => assert(counter == 6) }
//  }
//
//  "Table Commits Tasks" should "have three commits tasks" in {
//    daoCommitTasks.countCommitTasks(s) map { counter => assert(counter == 4) }
//  }
//
//  "LastestCommit revision" should "be three" in {
//    daoCommits.actionLatestRevision(s) map { counter => assert(counter.getOrElse(-1) == 3) }
//  }
//
//}
