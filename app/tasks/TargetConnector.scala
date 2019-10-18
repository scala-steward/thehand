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

import java.net.URL

import network.HttpBasicAuth
import play.api.libs.json._
import telemetrics.HandLogger

import scala.io.Source
import scala.io.Source.fromURL
import scala.util.{ Failure, Success, Try }

object TargetConnector {
  def apply(url: String, user: String, password: String): TargetConnector = new TargetConnector(url, user, password)
}

class TargetConnector(url: String, user: String, password: String) extends TaskConnector {
  lazy val token: String = "&token=" + (auth() getOrElse "")

  private def authConnect(): Try[String] = Try {
    val connection = new URL(url + "/Authentication/?&format=json").openConnection
    connection.setRequestProperty(HttpBasicAuth.AUTHORIZATION, HttpBasicAuth.getHeader(user, password))
    Source.fromInputStream(connection.getInputStream).mkString
  }

  private def parseToken(t: String): Option[String] = {
    val json = Json.parse(t)
    (json \ "Token").asOpt[String]
  }

  private def auth(): Option[String] = {
    val response = authConnect()
    response match {
      case Success(json) =>
        HandLogger.debug("authenticate")
        parseToken(json)
      case Failure(e) =>
        HandLogger.error("error in authentication " + e.getMessage)
        None
    }
  }

  def jsonData(url: String, options: String): String = {
    val response = Try {
      val jsonData = fromURL(url + "/?" + options + token + "&format=json")
      val jsonDataString = jsonData.mkString
      jsonData.close()
      jsonDataString
    }
    response match {
      case Success(json) =>
        HandLogger.debug("get data from " + url)
        json
      case Failure(e) =>
        HandLogger.error("error " + e.getMessage)
        ""
    }
  }

  def assignable(id: Long): String = {
    jsonData(url + "/Assignables/" + id.toString, "&include=[Id,Project,EntityType,TimeSpent,Effort,CustomFields]")
  }

  def bugs(id: Long): String = {
    jsonData(url + "/Bugs/" + id.toString, "&include=[Id,Project,EntityType,Effort,TimeSpent,UserStory,CustomFields]")
  }

  def customFields(id: Long): String = {
    jsonData(url + "/Assignables/" + id.toString, "&include[CustomFields]")
  }
}

