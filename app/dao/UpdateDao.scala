package dao

import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import cross.{ScmConnector, SvnConnector, SvnConnectorFactory, SvnRepositoryData}
import org.tmatesoft.svn.core.{SVNException, SVNLogEntry}
import slick.jdbc.JdbcProfile
import thehand.telemetrics.HandLogger
import thehand.{TaskParser, TaskParserCharp}
import thehand.tasks.{TargetConnector, TaskConnector}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton()
class UpdateDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: play.api.Configuration)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  private implicit val target: TaskConnector = TargetConnector(
    conf.getString("target.url").get,
    conf.getString("target.user").get,
    conf.getString("target.pass").get)

  private def loadSvnRepository(task: TaskConnector, confName: String) = {
    lazy val suffix = Suffix(conf.getString(confName + ".database_suffix").getOrElse("_"))

    implicit val parser: TaskParser = TaskParserCharp(
      conf.getString(confName + ".task_model.patternParser").get,
      conf.getString(confName + ".task_model.patternSplit").get,
      conf.getString(confName + ".task_model.separator").get)

    lazy val rep = new SvnConnectorFactory {}
    lazy val repository: Future[SvnConnector] = rep.connect(
      conf.getString(confName + ".url").get,
      conf.getString(confName + ".user").get,
      conf.getString(confName + ".pass").get)

    // hiro wrong place
    val b = new Bootstrap(dbConfigProvider)
    b.createSchemas(suffix)

    repository.flatMap(r => Future.successful(new SvnRepositoryData(dbConfigProvider, r, suffix)))
  }

  private def updateRepositoryAuto(confName: String) = {
    val repository = loadSvnRepository(target, confName)
    repository.flatMap(rep => rep.updateAuto())
  }

  private def updateRepositoryRange(confName: String, from: Long, to: Long) = {
    val repository = loadSvnRepository(target, confName)
    repository.flatMap(rep => rep.updateRange((from, to)))
  }

  def update(suffix: Suffix, from: Option[Long], to: Option[Long]) = {
    if (from.isDefined && to.isDefined) updateRepositoryRange(suffix.suffix, from.get, to.get)
  }

  def updateAll(): Future[Seq[Int]] = {
    def repositories = Seq(
      updateRepositoryAuto("eb_"),
      updateRepositoryAuto("qb_"),
      updateRepositoryAuto("nu_")
    )
    Future.sequence(repositories).map(_.flatten)
  }

}
