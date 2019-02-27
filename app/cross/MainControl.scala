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

import com.typesafe.config.{Config, ConfigFactory}
import org.tmatesoft.svn.core.SVNLogEntry
import slick.jdbc.JdbcProfile
import thehand.telemetrics.HandLogger
import thehand.{TaskParser, TaskParserCharp}
import thehand.tasks.{TargetConnector, TaskConnector}

import scala.concurrent.ExecutionContextExecutor
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

class MainControl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) {
  implicit val conf: Config = ConfigFactory.load()
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val jdbcProfile: JdbcProfile = slick.jdbc.PostgresProfile

  // update projects
  def update(repositories: Seq[String]): Unit = {
    lazy val target: TaskConnector = TargetConnector(
      conf.getString("target.url"),
      conf.getString("target.user"),
      conf.getString("target.pass"))

    def loadSvnRepository(task: TaskConnector, confName: String): Option[SvnRepositoryData] = {
      val parser: TaskParser = TaskParserCharp(
        conf.getString(confName + ".task_model.patternParser"),
        conf.getString(confName + ".task_model.patternSplit"),
        conf.getString(confName + ".task_model.separator"))

      lazy val rep = new SvnConnectorFactory {}
      lazy val repository: Option[ScmConnector[SVNLogEntry]] = rep.connect(
        conf.getString(confName + ".url"),
        conf.getString(confName + ".user"),
        conf.getString(confName + ".pass"))

      repository match {
        case Some(r) => Some(new SvnRepositoryData(dbConfigProvider, task, r, parser))
        case None => None
      }
    }

    def updateRepository(confName: String): Unit = {
      HandLogger.info("updating " + confName)
      val repository = loadSvnRepository(target, confName)
      repository.foreach { rep =>
        rep.updateAuto()
      }
    }

    repositories.foreach(updateRepository)
  }

  lazy val repositories = Seq(
    "repository_eberick",
    "repository_qibulder",
    "repository_qi4d"
  )
  update(repositories)

}