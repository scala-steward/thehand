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

import java.sql.Timestamp

import org.tmatesoft.svn.core.{SVNLogEntry, SVNLogEntryPath}
import tasks.TaskParser
import models._

import scala.collection.JavaConverters._

class SvnExtractor(parser: TaskParser) extends ScmExtractor[SVNLogEntry] {
  def extractTasks(data: Seq[SVNLogEntry]): Seq[Long] = {
    data
      .flatMap(s => parser.convert(Some(s.getMessage)))
      .distinct
  }

  def extractAuthors(data: Seq[SVNLogEntry]): Seq[Author] = {
    data.map(_.getAuthor)
      .distinct.map(s => Author(s))
  }

  def extractCommits(data: Seq[SVNLogEntry]): Seq[(CommitEntry, String)] = {
    data.map(s =>
      (CommitEntry(Some(s.getMessage), Some(new Timestamp(s.getDate.getTime)), s.getRevision, 0), s.getAuthor))
  }

  def extractFiles(data: Seq[SVNLogEntry], branchName: String): Seq[EntryFile] = {
    data.flatMap(_.getChangedPaths.asScala.values.toSeq)
      .map(_.getPath.replace(branchName, ""))
      .distinct
      .map(EntryFile(_))
  }

  def extractCommitsFiles(data: Seq[SVNLogEntry]): Seq[(Seq[CommitEntryWriter], Long)] = {
    def extractCommitEntryFile(s: SVNLogEntryPath): CommitEntryWriter = {
      CommitEntryWriter(CommitEntryFile(Some(s.getType.toInt), None, Some(s.getCopyRevision), 0, 0), s.getPath, s.getCopyPath)
    }
    data.map(c => (c.getChangedPaths.asScala.values.toSeq.map(extractCommitEntryFile), c.getRevision))
  }

  def extractCommitsTasks(data: Seq[SVNLogEntry]): Seq[CommitTasks] = {
    def extractCommitTasks(s: SVNLogEntry): Seq[CommitTasks] = {
      parser.convert(Some(s.getMessage)).map(CommitTasks(_, s.getRevision))
    }
    data.flatMap(extractCommitTasks)
  }
}
