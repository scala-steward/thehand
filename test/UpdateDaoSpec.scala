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

import models.{Author, CommitEntry, CommitEntryFile, CommitTasks, EntryFile, Suffix, Task}
import org.specs2.concurrent.ExecutionEnv

class UpdateDaoSpec(implicit ee: ExecutionEnv)  extends DaoSpec {
  val suffix = Suffix("UpdateDaoSpec_")
  fixture.populate(suffix)

  "After populate suffix db with fixture data" should {
    "list task tablle must have five tasks" in {
      val tasks = fixture.daoTasks.list(suffix)
      tasks must haveSize[Seq[Task]](5).await
    }
    "list author table must have three authors" in {
      val counter = fixture.daoAuthors.list(suffix)
      counter must haveSize[Seq[Author]](3).await
    }
    "list commit table must have three commits" in {
      val counter = fixture.daoCommits.list(suffix)
      counter must haveSize[Seq[CommitEntry]](3).await
    }
    "list commit entry table with a revision three must return one" in {
      val counter = fixture.daoCommits.list(suffix, Some(3))
      counter must haveSize[Seq[CommitEntry]](1).await
    }
    "list commit entry table with a revision nine must return none" in {
      val counter = fixture.daoCommits.list(suffix, Some(9))
      counter must haveSize[Seq[CommitEntry]](0).await
    }
    "list commit entry table without revision id return all" in {
      val counter = fixture.daoCommits.list(suffix, None)
      counter must haveSize[Seq[CommitEntry]](3).await
    }
    "list table files must have three files" in {
      val counter = fixture.daoFiles.list(suffix)
      counter must haveSize[Seq[EntryFile]](3).await
    }
    "list table commit entry file must have six commits files" in {
      val counter = fixture.daoCommitFiles.list(suffix)
      counter must haveSize[Seq[CommitEntryFile]](6).await
    }
    "list table commit tasks must have three commits tasks" in {
      val counter = fixture.daoCommitTasks.list(suffix)
      counter must haveSize[Seq[CommitTasks]](4).await
    }
    "last commit revision id must be three" in {
      val last = fixture.daoCommits.actionLatestRevision(suffix)
      last must beEqualTo[Option[Int]](Some(3)).await
    }
    "repeat insert a commitTask update record" in {
      val insert = fixture.daoCommitTasks.insert(ExtractorFixture.commitTaskChange, suffix)
      insert must beEqualTo[Seq[Int]](Seq(1)).await
    }
  }
}
