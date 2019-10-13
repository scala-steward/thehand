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

package tasks

import models.{CustomFields, Task}
import play.api.libs.json.{JsValue, Json}
import telemetrics.HandLogger

import scala.util.{Failure, Success, Try}

object ProcessTargetConnector {
  def apply(t: TaskConnector): ProcessTargetConnector = new ProcessTargetConnector(t)
}

class ProcessTargetConnector(t: TaskConnector) extends TaskProcessConnector {
  private def parseTaskJson(json: JsValue): Option[Task] = {
    val typeTask = (json \ "EntityType" \ "Name").validateOpt[String].getOrElse(None)
    val typeTaskId = (json \ "EntityType" \ "Id").validateOpt[Long].getOrElse(None)
    val timeSpend = (json \ "TimeSpent").validateOpt[Double].getOrElse(None)
    val parentId = (json \ "Project" \ "Id").validateOpt[Long].getOrElse(None)
    val userStoryId = (json \ "UserStory" \ "Id").validateOpt[Long].getOrElse(None)
    val id = (json \ "Id").validateOpt[Long].getOrElse(None)
    if (id.isDefined) Some(Task(typeTask, typeTaskId, userStoryId, timeSpend, parentId, id.get)) else None
  }

  private def filterRequestType(json: JsValue, field: String) : Boolean =
    (json \ "Name").validateOpt[String].getOrElse(None) match {
      case Some(name) => name == field
      case _ => false
    }

  private def getRequestType(request: collection.Seq[JsValue], field: String): Option[String] =
    request
      .filter(filterRequestType(_, field))
      .map(js => (js \ "Value")
        .validateOpt[String]
        .getOrElse(None)) match {
        case Seq(rType) => rType
        case _ => None
      }

  private def parseRequestType(json: JsValue, field: String) : Option[String] =
    (json \ "CustomFields").validateOpt[collection.Seq[JsValue]].getOrElse(None) match {
      case Some(request) => getRequestType(request, field)
      case _ => None
    }

  private def parseCustomFieldJson(json: JsValue, field: String): Option[CustomFields] =  {
    val id = (json \ "Id").validateOpt[Long].getOrElse(None)
    val requestType = parseRequestType(json, field)
    if (id.isDefined && requestType.isDefined) Some(CustomFields(requestType, field, id.get)) else None
  }

  def process(id: Long): Option[Task] = {
    callAndProcess(id, t.assignable, parseTaskJson) match {
      case Some(task) if (task.typeTask.contains("Bug")) => callAndProcess(id, t.bugs, parseTaskJson)
      case task => task
    }
  }

  private def callAndProcess(id: Long, f: Long => String, g: JsValue => Option[Task]): Option[Task] = {
    Try {
      Json.parse(f(id))
    } match {
      case Success(s) => g(s)
      case Failure(e) =>
        HandLogger.error("error in parse the json task data " + e)
        None
    }
  }

  def processCustomFields(id: Long, field: String): Option[CustomFields] = {
    Try {
      Json.parse(t.customFields(id))
    } match {
      case Success(s) => parseCustomFieldJson(s, field)
      case Failure(e) =>
        HandLogger.error("error in parse the custom field data " + e)
        None
    }
  }
}
