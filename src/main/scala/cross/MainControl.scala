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
import thehand.schemas.{ReportsDao, RepositoryDao}
import org.tmatesoft.svn.core.SVNLogEntry
import slick.jdbc.JdbcProfile
import telemetrics.HandLogger
import thehand.scm.{ReportExchange, ScmConnector, SvnConnectorFactory, SvnRepositoryData}
import thehand.{TaskParser, TaskParserCharp}
import thehand.tasks.{TargetConnector, TaskConnector}

import scala.concurrent.ExecutionContextExecutor

object MainControl extends App {
  implicit val conf: Config = ConfigFactory.load()
  implicit val context: ExecutionContextExecutor = scala.concurrent.ExecutionContext.fromExecutor(null)
  lazy val jdbcProfile: JdbcProfile = slick.jdbc.PostgresProfile

  // update projects
  def update(repositories: Seq[String]) = {
    lazy val target: TaskConnector = TargetConnector(
      conf.getString("target.url"),
      conf.getString("target.user"),
      conf.getString("target.pass"))

    def loadSvnRepository(task: TaskConnector, confName: String, confPath: String): Option[SvnRepositoryData] = {
      lazy val parser: TaskParser = TaskParserCharp(
        conf.getString(confName + ".task_model.patternParser"),
        conf.getString(confName + ".task_model.patternSplit"),
        conf.getString(confName + ".task_model.separator"))

      lazy val suffix = conf.getString(confName + ".database_suffix")
      lazy val dao: RepositoryDao = new RepositoryDao(jdbcProfile, confPath, suffix)

      lazy val rep = new SvnConnectorFactory {}
      lazy val repository: Option[ScmConnector[SVNLogEntry]] = rep.connect(
        conf.getString(confName + ".url"),
        conf.getString(confName + ".user"),
        conf.getString(confName + ".pass"))

      repository match {
        case Some(r) => Some(new SvnRepositoryData(dao, task, r, parser))
        case None => None
      }
    }

    def updateRepository(confName: String) = {
      HandLogger.info("updating " + confName)
      val repository = loadSvnRepository(target, confName, "dbconfig")
      repository.foreach { rep =>
        rep.updateAuto()
        rep.close()
      }
    }

    repositories.map(updateRepository(_))
  }

  lazy val repositories = Seq(
    "repository_eberick",
    "repository_qibulder",
    "repository_qi4d"
  )
  update(repositories)

  // generate reports
  def generateRepositoryReport(repository: String)= {
    lazy val suffix = conf.getString(repository+".database_suffix")
    lazy val dao: ReportsDao = new ReportsDao(jdbcProfile, "dbconfig", suffix)
    val reports = ReportExchange(dao, repository)
    reports.reportFilesBugsCounterCvs
    reports.authorsReports
    reports.close
  }

  repositories.map(generateRepositoryReport)
}