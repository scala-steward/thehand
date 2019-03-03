package dao

import models._
import play.api.db.slick.HasDatabaseConfigProvider
import scm.{ SvnConnector, SvnConnectorFactory, SvnRepositoryData }
import slick.jdbc.JdbcProfile
import tasks.{ TaskParser, TaskParserCharp }
import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import tasks.{ TargetConnector, TaskConnector }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton()
class UpdateDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: play.api.Configuration)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  private implicit val target: TaskConnector = TargetConnector(
    conf.get[String]("target.url"),
    conf.get[String]("target.user"),
    conf.get[String]("target.pass"))

  private def loadSvnRepository(confName: String) = {
    lazy val suffix = Suffix(conf.get[String](confName + ".database_suffix"))

    implicit val parser: TaskParser = TaskParserCharp(
      conf.get[String](confName + ".task_model.patternParser"),
      conf.get[String](confName + ".task_model.patternSplit"),
      conf.get[String](confName + ".task_model.separator"))

    lazy val rep = new SvnConnectorFactory {}
    lazy val repository: Future[SvnConnector] = rep.connect(
      conf.get[String](confName + ".url"),
      conf.get[String](confName + ".user"),
      conf.get[String](confName + ".pass"))

    // hiro wrong place
    val b = new BootstrapDAO(dbConfigProvider)
    b.createSchemas(suffix)

    repository.flatMap(r => Future.successful(new SvnRepositoryData(dbConfigProvider, r, suffix)))
  }

  private def updateRepositoryAuto(confName: String) = {
    val repository = loadSvnRepository(confName)
    repository.flatMap(rep => rep.updateAuto())
  }

  private def updateRepositoryRange(confName: String, from: Long, to: Long) = {
    val repository = loadSvnRepository(confName)
    repository.flatMap(rep => rep.updateRange((from, to)))
  }

  def update(suffix: Suffix, from: Option[Long], to: Option[Long]) = {
    if (from.isDefined && to.isDefined) updateRepositoryRange(suffix.suffix, from.get, to.get)
  }

  def updateAll(): Future[Seq[Int]] = {
    def repositories = Seq(
      updateRepositoryAuto("eb_"),
      updateRepositoryAuto("qb_"),
      updateRepositoryAuto("nu_"))
    Future.sequence(repositories).map(_.flatten)
  }

}
