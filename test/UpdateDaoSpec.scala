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
//import org.scalatest.AsyncFeatureSpec
//import org.specs2.Specification
//import play.api.Application
////import play.api.Application
//import play.api.db.slick.DatabaseConfigProvider
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.test.{ PlaySpecification, WithApplication }
//import telemetrics.HandLogger
//
//import scala.concurrent.{ ExecutionContext, Future }
//import scala.util.{ Failure, Success }
//import play.db.NamedDatabase
//
//class UpdateDaoSpec extends PlaySpecification with AsyncFeatureSpec {
//
//  implicit val context = ExecutionContext.Implicits.global
//
//  implicit lazy val app = new GuiceApplicationBuilder().
//    configure(
//      "slick.dbs.mydb.driver" -> "slick.driver.H2Driver$",
//      "slick.dbs.mydb.db.driver" -> "org.h2.Driver",
//      "slick.dbs.mydb.db.url" -> "jdbc:h2:mem:blah;",
//      "slick.dbs.mydb.db.user" -> "test",
//      "slick.dbs.mydb.db.password" -> "").build
//
//  val extractor = new ExtractorFixture()
//  val s = Suffix("echo")
//
//  val daoTasks = Application.instanceCache[TaskDAO].apply(app)
//  daoTasks.insert(extractor.extractTasks, s) onComplete {
//    case Success(_) => HandLogger.debug("correct write tasks")
//    case Failure(e) => HandLogger.error("error in writing tasks " + e.getMessage)
//  }
//
//  //  val daoAuthor = new Application.instanceCache[AuthorDAO].apply(app)
//  //  daoAuthor.insert(extractor.extractAuthors, s) onComplete {
//  //    case Success(_) => HandLogger.debug("correct write authors")
//  //    case Failure(e) => HandLogger.error("error in writing authors " + e.getMessage)
//  //  }
//  //
//  //  val daoCommits = new Application.instanceCache[CommitDAO].apply(app)
//  //  daoCommits.insert(extractor.extractCommits, s) onComplete {
//  //    case Success(_) => HandLogger.debug("correct create commits")
//  //    case Failure(e) => HandLogger.error("error in writing commits " + e.getMessage)
//  //  }
//  //
//  //  val daoFiles = new Application.instanceCache[EntryFileDAO].apply(app)
//  //  daoFiles.insert(extractor.extractFiles, s) onComplete {
//  //    case Success(_) => HandLogger.debug("correct create files")
//  //    case Failure(e) => HandLogger.error("error in writing files " + e.getMessage)
//  //  }
//
//  val daoCommitFiles = Application.instanceCache[CommitEntryFileDAO].apply(app)
//  daoCommitFiles.insert(extractor.extractCommitsFiles, s) onComplete {
//    case Success(_) => HandLogger.debug("correct create commits files")
//    case Failure(e) => HandLogger.error("error in writing commits files " + e.getMessage)
//  }
//
//  val daoCommitTasks = Application.instanceCache[CommitTaskDAO].apply(app)
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
//  //  "Table Authors" should "have three authors" in {
//  //    daoAuthor.countAuthors(s) map { counter => assert(counter == 3) }
//  //  }
//  //
//  //  "Table Commits" should "have three commits" in {
//  //    daoCommits.countCommits(s) map { counter => assert(counter == 3) }
//  //  }
//  //
//  //  "Table Files" should "have three files" in {
//  //    daoFiles.countFiles(s) map { counter => assert(counter == 3) }
//  //  }
//
//  "Table Commits Files" should "have eight commits files" in {
//    daoCommitFiles.countCommitsFiles(s) map { counter => assert(counter == 6) }
//  }
//
//  "Table Commits Tasks" should "have three commits tasks" in {
//    daoCommitTasks.countCommitTasks(s) map { counter => assert(counter == 4) }
//  }
//
//  //  "LastestCommit revision" should "be three" in {
//  //    daoCommits.actionLatestRevision(s) map { counter => assert(counter.getOrElse(-1) == 3) }
//  //  }
//
//}
