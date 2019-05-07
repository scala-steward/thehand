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
import play.api.libs.json.{JsArray, JsValue, Json}
import telemetrics.HandLogger

import scala.util.{Failure, Success, Try}

object ProcessTargetConnector {
  def apply(t: TaskConnector): ProcessTargetConnector = new ProcessTargetConnector(t)
}

class ProcessTargetConnector(t: TaskConnector) {
  private def parseAssignableJson(json: JsValue): Option[Task] = {
    val typeTask = (json \ "EntityType" \ "Name").validateOpt[String].get
    val typeTaskId = (json \ "EntityType" \ "Id").validateOpt[Long].get
    val timeSpend = (json \ "TimeSpent").validateOpt[Double].get
    val parentId = (json \ "Project" \ "Id").validateOpt[Long].get
    val id = (json \ "Id").validateOpt[Long].get
    if (id.isDefined) Some(Task(typeTask, typeTaskId, timeSpend, parentId, id.get)) else None
  }

  private def filterRequestType(json: JsValue) : Boolean = {
    val name = (json \ "Name").validateOpt[String].get
    name.isDefined && name.get == "Request Type"
  }

  private def parseCustomFieldJson(json: JsValue): Option[CustomFields] =  {
    val requestType =
      (json \ "CustomFields")
      .validateOpt[JsArray]
      .get
      .filter(filterRequestType)
      .map(js => (js \ "Value").validateOpt[String])
      .map(_.get)
      .getOrElse(None)

    val id = (json \ "Id").validateOpt[Long].get
    if (id.isDefined && requestType.isDefined) Some(CustomFields(requestType, id.get)) else None
  }

  def process(id: Long): Option[Task] = {
    Try {
      Json.parse(t.assignables(id))
    } match {
      case Success(s) => parseAssignableJson(s)
      case Failure(e) =>
        HandLogger.error("error in parse the json task data " + e)
        None
    }
  }

  def processCustomFields(id: Long): Option[CustomFields] = {
    Try {
      Json.parse(t.customFields(id))
    } match {
      case Success(s) => parseCustomFieldJson(s)
      case Failure(e) =>
        HandLogger.error("error in parse the custom field data " + e)
        None
    }
  }
}
