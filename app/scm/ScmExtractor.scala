package scm

import models.{Author, CommitEntry, CommitEntryWriter, CommitTasks, EntryFile}

trait ScmExtractor[T] {
  def extractTasks(data: Seq[T]): Seq[Long]
  def extractAuthors(data: Seq[T]): Seq[Author]
  def extractCommits(data: Seq[T]): Seq[(CommitEntry, String)]
  def extractFiles(data: Seq[T]): Seq[EntryFile]
  def extractCommitsFiles(data: Seq[T]): Seq[(Seq[CommitEntryWriter], Long)]
  def extractCommitsTasks(data: Seq[T]): Seq[CommitTasks]
}