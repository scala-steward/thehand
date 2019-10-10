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
  private def parseAssignableJson(json: JsValue): Option[Task] = {
    val typeTask = (json \ "EntityType" \ "Name").validateOpt[String].get
    val typeTaskId = (json \ "EntityType" \ "Id").validateOpt[Long].get
    val timeSpend = (json \ "TimeSpent").validateOpt[Double].get
    val parentId = (json \ "Project" \ "Id").validateOpt[Long].get
    val id = (json \ "Id").validateOpt[Long].get
    if (id.isDefined) Some(Task(typeTask, typeTaskId, timeSpend, parentId, id.get)) else None
  }

  private def filterRequestType(json: JsValue, field: String) : Boolean = {
    val name = (json \ "Name").validateOpt[String].get
    name.isDefined && name.get == field
  }

  private def getRequestType(request: Option[Seq[JsValue]], field: String) = {
    val requestType = request.get.filter(filterRequestType(_, field))
      .map(js => (js \ "Value").validateOpt[String].get)
    if (requestType.nonEmpty) requestType.head else None
  }

  private def parseRequestType(json: JsValue, field: String) : Option[String] = {
    def request = (json \ "CustomFields").validateOpt[Seq[JsValue]].get
    if (request.isDefined) getRequestType(request, field) else None
  }

  private def parseCustomFieldJson(json: JsValue, field: String): Option[CustomFields] =  {
    val id = (json \ "Id").validateOpt[Long].get
    val requestType = parseRequestType(json, field)
    if (id.isDefined && requestType.isDefined) Some(CustomFields(requestType, field, id.get)) else None
  }

  def process(id: Long): Option[Task] = {
    Try {
      Json.parse(t.assignable(id))
    } match {
      case Success(s) => parseAssignableJson(s)
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
