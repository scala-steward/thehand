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

import models._

class ExtractorFixture() {
  val extractTasks: Seq[Task] =
    Seq(
      Task(Some("Task"), Some(5L), Some(20), None, 1),
      Task(Some("Task"), Some(5L), Some(20), None, 2),
      Task(Some("Bug"), Some(8L), Some(20), Some(1), 3),
      Task(Some("Bug"), Some(8L), Some(20), Some(2), 4),
      Task(Some("Bug"), Some(8L), Some(20), Some(2), 5))

  val extractAuthors: Seq[Author] = Seq(Author("john"), Author("philips"), Author("thomas"))

  private lazy val commitOne = CommitEntry(Some("Task #1"), None, 1, 0)
  private lazy val commitTwo = CommitEntry(Some("Task #2"), None, 2, 0)
  private lazy val commitThree = CommitEntry(Some("Bug #3"), None, 2, 0)
  private lazy val commitFour = CommitEntry(Some("Bug #4"), None, 2, 0)
  private lazy val commitFive = CommitEntry(Some("Bug #5"), None, 3, 0)

  val extractCommits: Seq[(CommitEntry, String)] =
    Seq(
      (commitOne, "john"),
      (commitTwo, "philips"),
      (commitThree, "philips"),
      (commitFour, "philips"),
      (commitFive, "thomas"),
      (commitOne, "john"))

  def extractFiles: Seq[EntryFile] =
    Seq(EntryFile("/zip"), EntryFile("/zap"), EntryFile("/zop"), EntryFile("/zip"))

  private lazy val commitFilesOne =
    Seq(
      CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zip", ""),
      CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zap", ""),
      CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zop", ""))
  private lazy val commitFilesTwo =
    Seq(
      CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(2), 0, 0), "/zip", ""),
      CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(2), 0, 0), "/zap", ""))
  private lazy val commitFilesThree =
    Seq(CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(3), 0, 0), "/zip", ""))
  private lazy val commitFilesFour =
    Seq(CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(4), 0, 0), "/zip", ""))
  private lazy val commitFilesFive =
    Seq(CommitEntryWriter(CommitEntryFile(Some('D'), None, Some(5), 0, 0), "/zip", ""))

  val extractCommitsFiles: Seq[(Seq[CommitEntryWriter], Long)] = Seq(
    (commitFilesOne, 1L),
    (commitFilesTwo, 2L),
    (commitFilesThree, 2L),
    (commitFilesFour, 2L),
    (commitFilesFive, 3L))

  val extractCommitsTasks: Seq[CommitTasks] = Seq(CommitTasks(1, 1), CommitTasks(2, 3), CommitTasks(3, 2), CommitTasks(5, 3))
}
