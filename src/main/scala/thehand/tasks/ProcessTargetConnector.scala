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

package thehand.tasks

import thehand.schemas.Task
import play.api.libs.json.{JsValue, Json}
import telemetrics.HandLogger

import scala.util.{Failure, Success, Try}

object ProcessTargetConnector {
  def apply(t: TaskConnector): ProcessTargetConnector = new ProcessTargetConnector(t)
}

class ProcessTargetConnector(t: TaskConnector) {
  private def parseTask(json: JsValue): Try[Task] = Try {
    val typeTask = (json \ "EntityType" \ "Name").validateOpt[String].get
    val typeTaskId = (json \ "EntityType" \ "Id").validateOpt[Long].get
    val timeSpend = (json \ "TimeSpent").validateOpt[Double].get
    val parentId = (json \ "Project" \ "Id").validateOpt[Long].get
    val id = (json \ "Id").validate[Long].get
    Task(typeTask, typeTaskId, timeSpend, parentId, id)
  }

  def parseJson(jsonValue: JsValue) : Option[Task] = {
    parseTask(jsonValue) match {
      case Success(task) => Some(task)
      case Failure(e) =>
        HandLogger.error("error in parse the json task data " + e)
        None
    }
  }

  def process(id: Long) : Option[Task]  = {
    Try { Json.parse(t.assignables(id)) } match {
      case Success(s) => parseJson(s)
      case Failure(e) => HandLogger.error("error in parse the json task data " + e)
        None
    }
  }
}
