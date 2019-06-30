package dao

import models._
import play.api.db.slick.HasDatabaseConfigProvider
import scm.{ScmRepositoryData, SvnConnector, SvnConnectorFactory, SvnExtractor}
import slick.jdbc.JdbcProfile
import tasks.{ProcessTargetConnector, TargetConnector, TaskConnector, TaskParser, TaskParserCharp}
import javax.inject.Inject
import org.tmatesoft.svn.core.SVNLogEntry
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class UpdateDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: play.api.Configuration)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {


  private def loadSvnRepository(confName: String) = {
    lazy val suffix = DatabeSuffix(conf.get[String](confName + ".database_suffix"))

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
    val b = new BootDAO(dbConfigProvider)
    b.createSchemas(suffix)

    val taskConnector: TaskConnector = TargetConnector(
      conf.get[String]("target.url"),
      conf.get[String]("target.user"),
      conf.get[String]("target.pass"))

    val extractor = new SvnExtractor(parser)
    val taskProcessor = ProcessTargetConnector(taskConnector)

    repository.flatMap { r =>
      Future.successful(new ScmRepositoryData[SVNLogEntry](dbConfigProvider, r, extractor, taskProcessor, suffix))
    }
  }

  private def updateRepositoryAuto(confName: String) = {
    val repository = loadSvnRepository(confName)
    repository.flatMap(rep => rep.updateAuto())
  }

  private def updateRepositoryRange(confName: String, from: Long, to: Long) = {
    val repository = loadSvnRepository(confName)
    repository.flatMap(rep => rep.updateRange((from, to)))
  }

  def update(suffix: DatabeSuffix, from: Option[Long], to: Option[Long]): Future[Seq[Int]] = {
    updateRepositoryRange(suffix.suffix, from.getOrElse(-1), to.getOrElse(-1))
  }

  def updateAll(): Future[Seq[Int]] = {
    val repSuffixes = conf.getOptional[Seq[String]]("repos").getOrElse(Seq())
    val repositories = repSuffixes.map(updateRepositoryAuto)
    Future.sequence(repositories).map(_.flatten)
  }

  def updateCustomFields(suffix: DatabeSuffix, field: String, from: Option[Long], to: Option[Long]): Future[Seq[Int]] = {
    val repository = loadSvnRepository(suffix.suffix)
    repository.flatMap(rep => rep.updateCustomFields(field, from.getOrElse(-1), to.getOrElse(-1)))
  }

}
