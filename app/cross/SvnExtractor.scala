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

package cross

import org.joda.time.DateTime
import org.tmatesoft.svn.core.{SVNLogEntry, SVNLogEntryPath}
import thehand.TaskParser
import models._

import scala.collection.JavaConverters._

class SvnExtractor(val svnData: Seq[SVNLogEntry], parser: TaskParser) {
  def extractTasks: Seq[Long] = {
    svnData
      .flatMap(s => parser.convert(Some(s.getMessage)))
      .distinct
  }

  def extractAuthors: Seq[Author] = {
    svnData.map(_.getAuthor)
      .distinct.map(s => Author(s))
  }

  def extractCommits: Seq[(CommitEntry, String)] = {
    svnData.map(s =>
      (CommitEntry(Some(s.getMessage), Some(new DateTime(s.getDate)), s.getRevision, 0), s.getAuthor))
  }

  def extractFiles: Seq[EntryFile] = {
    svnData.flatMap(_.getChangedPaths.asScala.values.toSeq)
      .map(_.getPath)
      .distinct
      .map(EntryFile(_))
  }

  def extractCommitsFiles: Seq[(Seq[CommitEntryWriter], Long)] = {
    def extractCommitEntryFile(s: SVNLogEntryPath): CommitEntryWriter = {
      CommitEntryWriter(CommitEntryFile(Some(s.getType.toInt), None, Some(s.getCopyRevision), 0, 0), s.getPath, s.getCopyPath)
    }
    svnData.map(c => (c.getChangedPaths.asScala.values.toSeq.map(extractCommitEntryFile), c.getRevision))
  }

  def extractCommitsTasks: Seq[CommitTasks] = {
    def extractCommitTasks(s: SVNLogEntry): Seq[CommitTasks] = {
      parser.convert(Some(s.getMessage)).map(CommitTasks(_, s.getRevision))
    }
    svnData.flatMap(extractCommitTasks)
  }
}
