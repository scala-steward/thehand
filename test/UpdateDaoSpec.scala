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

import dao._
import models.Suffix
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class UpdateDaoSpec(implicit ee: ExecutionEnv)  extends PlaySpecification with FutureMatchers {

  implicit val context = ExecutionContext.Implicits.global
  private val app: Application = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.mydb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.mydb.db.driver" -> "org.h2.Driver",
      "slick.dbs.mydb.db.url" -> "jdbc:h2:mem:blah;",
      "slick.dbs.mydb.db.user" -> "test",
      "slick.dbs.mydb.db.password" -> "").build

  private val suffix = Suffix("suffix_")

  private val daoBootstrap: BootstrapDAO = Application.instanceCache[BootstrapDAO].apply(app)
  daoBootstrap.createSchemas(suffix)

  private val daoTasks = Application.instanceCache[TaskDAO].apply(app)
  private val daoAuthors = Application.instanceCache[AuthorDAO].apply(app)
  private val daoCommits = Application.instanceCache[CommitDAO].apply(app)
  private val daoFiles = Application.instanceCache[EntryFileDAO].apply(app)
  private val daoCommitFiles = Application.instanceCache[CommitEntryFileDAO].apply(app)
  private val daoCommitTasks = Application.instanceCache[CommitTaskDAO].apply(app)

  private def populate() = {
    val insertAll = for {
      _ <- daoTasks.insert(ExtractorFixture.extractTasks, suffix)
      _ <- daoAuthors.insert(ExtractorFixture.extractAuthors, suffix)
      _ <- daoCommits.insert(ExtractorFixture.extractCommits, suffix)
      _ <- daoFiles.insert(ExtractorFixture.extractFiles, suffix)
      _ <- daoCommitTasks.insert(ExtractorFixture.extractCommitsTasks, suffix)
      c <- daoCommitFiles.insert(ExtractorFixture.extractCommitsFiles, suffix)
    } yield c
    Await.result(insertAll, 2 seconds)
  }
  populate()


  "Table tasks" should {
    "have five tasks" in {
      val tasks = daoTasks.countTasks(suffix)
      tasks must beEqualTo[Int](5).await
    }
  }

  "Table authors" should {
    "have three authors" in {
      val counter = daoAuthors.countAuthors(suffix)
      counter must beEqualTo[Int](3).await
    }
  }

  "Table commits" should {
    "have three commits" in {
      val counter = daoCommits.countCommits(suffix)
      counter must beEqualTo[Int](3).await
    }
  }

  "Table files" should {
    "have three files" in {
      val counter = daoFiles.countFiles(suffix)
      counter must beEqualTo[Int](3).await
    }
  }

  "Table commits files" should {
    "have eight commits files" in {
      val counter = daoCommitFiles.countCommitsFiles(suffix)
      counter must beEqualTo[Int](6).await
    }
  }

  "Table commits tasks" should {
    "have three commits tasks" in {
      val counter = daoCommitTasks.countCommitTasks(suffix)
      counter must beEqualTo[Int](4).await
    }
  }

  "Table commits tasks" should {
    "have three commits tasks" in {
      val commitTasks = daoCommitTasks.countCommitTasks(suffix)
      commitTasks must beEqualTo[Int](4).await
    }
  }

  "Lastest commit revision" should {
    "be three" in {
      val last = daoCommits.actionLatestRevision(suffix)
      last must beEqualTo[Option[Int]](Some(3)).await
    }
  }


}
