package dao

import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import cross.{ScmConnector, SvnConnectorFactory, SvnRepositoryData}
import org.tmatesoft.svn.core.SVNLogEntry
import slick.jdbc.JdbcProfile
import thehand.telemetrics.HandLogger
import thehand.{TaskParser, TaskParserCharp}
import thehand.tasks.{TargetConnector, TaskConnector}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UpdateDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: play.api.Configuration)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  private lazy val target: TaskConnector = TargetConnector(
    conf.getString("target.url").get,
    conf.getString("target.user").get,
    conf.getString("target.pass").get)

  private def loadSvnRepository(task: TaskConnector, confName: String): Option[SvnRepositoryData] = {
    val parser: TaskParser = TaskParserCharp(
      conf.getString(confName + ".task_model.patternParser").get,
      conf.getString(confName + ".task_model.patternSplit").get,
      conf.getString(confName + ".task_model.separator").get)

    lazy val rep = new SvnConnectorFactory {}
    lazy val repository: Option[ScmConnector[SVNLogEntry]] = rep.connect(
      conf.getString(confName + ".url").get,
      conf.getString(confName + ".user").get,
      conf.getString(confName + ".pass").get)

    lazy val suffix = Suffix(conf.getString(confName + ".database_suffix").getOrElse("_"))

    val b = new Bootstrap(dbConfigProvider)
    b.createSchemas(suffix)

    repository match {
      case Some(r) => Some(new SvnRepositoryData(dbConfigProvider, task, r, parser, suffix))
      case None => None
    }
  }

  private def updateRepositoryAuto(confName: String) = {
    HandLogger.info("updating " + confName)
    val repository = loadSvnRepository(target, confName)
    repository.foreach { rep => rep.updateAuto() }
  }

  private def updateRepositoryRange(confName: String, from: Long, to: Long) = {
    HandLogger.info("updating " + confName)
    val repository: Option[SvnRepositoryData] = loadSvnRepository(target, confName)
    repository.foreach(rep => rep.updateRange(from, to))
  }

  private def update(repositories: Seq[String]): Unit = {
    repositories.foreach(updateRepositoryAuto)
  }

  def update(suffix: Suffix, from: Option[Long], to: Option[Long]) = {
    if (from.isDefined && to.isDefined) updateRepositoryRange(suffix.suffix, from.get, to.get)
    else HandLogger.error("updating range fail" + from.toString() + to.toString())
  }

  def update(suffix: Suffix, from: Long, to: Long) = {
    val repository: Option[SvnRepositoryData] = loadSvnRepository(target, suffix.suffix)
    repository.foreach(rep => rep.updateRange(from, to))
  }

  def updateAll(): Unit = {
    lazy val repositories = Seq(
      "eb_",
      "qb_",
      "nu_"
    )
    update(repositories)
  }

}
