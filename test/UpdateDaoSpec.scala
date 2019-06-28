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
import models.{Author, CommitEntry, CommitEntryFile, CommitTasks, EntryFile, Suffix, Task}
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
    "list five tasks" in {
      val tasks = daoTasks.list(suffix)
      tasks must haveSize[Seq[Task]](5).await
    }
  }

  "Table authors" should {
    "list three authors" in {
      val counter = daoAuthors.list(suffix)
      counter must haveSize[Seq[Author]](3).await
    }
  }

  "Table commits" should {
    "list three commits" in {
      val counter = daoCommits.list(suffix)
      counter must haveSize[Seq[CommitEntry]](3).await
    }
  }

  "Table commits" should {
    "return commit revision tree" in {
      val counter = daoCommits.list(suffix, Some(3))
      counter must haveSize[Seq[CommitEntry]](1).await
    }
  }

  "Table commits" should {
    "return tree commits if revision not passed" in {
      val counter = daoCommits.list(suffix, None)
      counter must haveSize[Seq[CommitEntry]](3).await
    }
  }

  "Table files" should {
    "list three files" in {
      val counter = daoFiles.list(suffix)
      counter must haveSize[Seq[EntryFile]](3).await
    }
  }

  "Table commits files" should {
    "list six commits files" in {
      val counter = daoCommitFiles.list(suffix)
      counter must haveSize[Seq[CommitEntryFile]](6).await
    }
  }

  "Table commits tasks" should {
    "list three commits tasks" in {
      val counter = daoCommitTasks.list(suffix)
      counter must haveSize[Seq[CommitTasks]](4).await
    }
  }

  "Last commit revision" should {
    "be three" in {
      val last = daoCommits.actionLatestRevision(suffix)
      last must beEqualTo[Option[Int]](Some(3)).await
    }
  }

  "Repeat insert a commitTask" should {
    "update record" in {
      val insert = daoCommitTasks.insert(ExtractorFixture.commitTaskChange, suffix)
      insert must beEqualTo[Seq[Int]](Seq(1)).await
    }
  }
}
