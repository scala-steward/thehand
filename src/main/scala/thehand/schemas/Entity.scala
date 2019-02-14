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

import org.joda.time.DateTime

final case class Author(
                         author: String,
                         id: Long = 0L)

final case class CommitEntry(message: Option[String],
                             date: Option[DateTime],
                             revision: Long,
                             authorId: Long,
                             id: Long = 0L)

final case class CommitEntryFile(typeModification: Option[Char],
                                 copyPath: Option[Long],
                                 copyRevision: Option[Long],
                                 pathId: Long,
                                 revisionId: Long,
                                 id: Long = 0L)

final case class CommitFileUnify(pathId: Long,
                                 revisionId: Long,
                                 id: Long = 0L)

final case class CommitTasks(taskId: Long,
                             commitId: Long,
                             id: Long = 0L)

final case class EntryFile(path: String,
                           id: Long = 0L)

final case class Task(
                       typeTask: Option[String],
                       typeTaskId: Option[Long],
                       timeSpend: Option[Double],
                       parentId: Option[Long],
                       taskId: Long,
                       id: Long = 0L)

final case class Parser(
                       name: String,
                       pattern: String,
                       split: String,
                       separator: String,
                       id: Long = 0L)

final case class TaskManager(
                            name: String,
                            user: String,
                            pass: String,
                            usr: String,
                            id: Long = 0L)

final case class Scm(
                      name: String,
                      user: String,
                      pass: String,
                      usr: String,
                      id: Long = 0L)

final case class Project(
                        name: String,
                        defaultMode: Option[Char],
                        scmId: Long,
                        taskManagerId: Long,
                        id: Long = 0L)

final case class CommitEntryWriter(
                                    commit: CommitEntryFile,
                                    path: String,
                                    pathCopy: String)