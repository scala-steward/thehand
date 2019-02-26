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

trait TaskTables {
  this: Profile => import profile.api._

  object TaskTable {
    def apply(tag: Tag, suffix: String): TaskTable = new TaskTable(tag, suffix)
  }

  final class TaskTable(tag: Tag, suffix: String) extends Table[Task](tag, suffix + "task") {
    def typeTask = column[Option[String]]("type_task")
    def typeTaskId = column[Option[Long]]("type_task_id")
    def timeSpend = column[Option[Double]]("time_spend")
    def parentId = column[Option[Long]]("parent_id")
    def taskId = column[Long]("task_id", O.Unique)
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (typeTask, typeTaskId, timeSpend, parentId, taskId, id).mapTo[Task]
  }
}


