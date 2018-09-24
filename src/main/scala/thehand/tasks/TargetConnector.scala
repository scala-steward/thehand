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

import java.net.URL

import network.HttpBasicAuth
import play.api.libs.json._
import telemetrics.HandLogger

import scala.io.Source
import scala.io.Source.fromURL
import scala.util.{Failure, Success, Try}

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
      case Success(json) => HandLogger.debug("authenticate")
        parseToken(json)
      case Failure(e) => HandLogger.error("error in authentication " + e.getMessage)
        None
    }
  }

  def jsonData(url: String, options: String): String = {
    val response = Try { fromURL(url +  "/?" + options + token + "&format=json").mkString }
    response match {
      case Success(json) => HandLogger.debug("get data from " + url)
        json
      case Failure(e) => HandLogger.error("error " + e.getMessage)
        ""
    }
  }

  def entityStates(id: String): String = {
    jsonData(url + "/EntityStates/" + id, "")
  }

  def assignables(id: Long, options: String = "[Id,Project,EntityType,Effort,TimeSpent]"): String = {
    jsonData(url + "/Assignables/" + id.toString, "&include=" + options)
  }

  def userId(options: String = "&include=[Id]"): String = {
    jsonData(url + "/Context/", options)
  }

  def bugs(id: Long, options: String): String = {
    jsonData(url + "/Bugs/" + id.toString, "&include=" + options)
  }

  def tasks(id: Long, options: String): String = {
    jsonData(url + "/Tasks/" + id.toString, "&include=" + options)
  }

  def generals(id: Long, options: String): String = {
    jsonData(url + "/Generals/" + id.toString, "&include=" + options)
  }

  def projects(id: Long, options: String): String = {
    jsonData(url + "/Projects/" + id.toString, "&include=" + options)
  }

  def features(id: Long, options: String): String = {
    jsonData(url + "/Features/" + id.toString, "&include=" + options)
  }

  def userStories(id: Long, options: String): String = {
    jsonData(url + "/UserStories/" + id.toString, "&include=" + options)
  }

  def userTasks(id: Long, take: Option[String], options: String = "[Name,Project,Description,EntityType,Effort,Priority,TimeSpent,EntityState]"): String = {
    val takeOption = if (take.isEmpty) "" else "&take=" + take
    jsonData(url + "/Users/" + id.toString + "/Assignables", "&include=" + options + takeOption)
  }
}


