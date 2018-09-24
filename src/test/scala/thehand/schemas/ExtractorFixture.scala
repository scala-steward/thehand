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

class ExtractorFixture() {
  private lazy val tasks =
    Task(Some("Task"), Some(5L), Some(20), None, 1)::
    Task(Some("Task"), Some(5L), Some(20), None, 2)::
    Task(Some("Bug"), Some(8L), Some(20), Some(1), 3)::
    Task(Some("Bug"), Some(8L), Some(20), Some(2), 4)::
    Task(Some("Bug"), Some(8L), Some(20), Some(2), 5)::Nil

  private lazy val authors = Author("john")::Author("philips")::Author("thomas")::Nil
  def extractTasks: Seq[Task] = tasks

  def extractAuthors: Seq[Author] = authors

  private lazy val commitOne = CommitEntry(Some("Task #1"), None, 1, 0)
  private lazy val commitTwo = CommitEntry(Some("Task #2"), None, 2, 0)
  private lazy val commitThree = CommitEntry(Some("Bug #3"), None, 2, 0)
  private lazy val commitFour = CommitEntry(Some("Bug #4"), None, 2, 0)
  private lazy val commitFive = CommitEntry(Some("Bug #5"), None, 3, 0)

  private lazy val commits =
    (commitOne,"john")::
    (commitTwo,"philips")::
    (commitThree,"philips")::
    (commitFour,"philips")::
    (commitFive,"thomas")::
    (commitOne,"john")::Nil
  def extractCommits: Seq[(CommitEntry, String)] = commits

  private lazy val files = EntryFile("/zip")::EntryFile("/zap")::EntryFile("/zop")::EntryFile("/zip")::Nil
  def extractFiles: Seq[EntryFile] = files

  private lazy val commitFilesOne =
    CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zip", "")::
    CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zap", "")::
    CommitEntryWriter(CommitEntryFile(Some('A'), None, Some(1), 0, 0), "/zop", "")::Nil
  private lazy val commitFilesTwo =
    CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(2), 0, 0), "/zip", "")::
    CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(2), 0, 0), "/zap", "")::Nil
  private lazy val commitFilesThree =
    CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(3), 0, 0), "/zip", "")::Nil
  private lazy val commitFilesFour =
    CommitEntryWriter(CommitEntryFile(Some('M'), None, Some(4), 0, 0), "/zip", "")::Nil
  private lazy val commitFilesFive =
    CommitEntryWriter(CommitEntryFile(Some('D'), None, Some(5), 0, 0), "/zip", "")::Nil

  def extractCommitsFiles: Seq[(Seq[CommitEntryWriter], Long)] =
    (commitFilesOne, 1L)::
    (commitFilesTwo, 2L)::
    (commitFilesThree, 2L)::
    (commitFilesFour, 2L)::
    (commitFilesFive, 3L)::Nil

  private lazy val commitTasks = CommitTasks(1, 1)::CommitTasks(2, 3)::CommitTasks(3, 2)::CommitTasks(5, 3)::Nil
  def extractCommitsTasks: Seq[CommitTasks] = commitTasks
}
