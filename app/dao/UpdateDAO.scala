package dao

import conf.RepoConf
import models._
import play.api.db.slick.HasDatabaseConfigProvider
import scm.{ScmRepositoryData, SvnConnector, SvnConnectorFactory, SvnExtractor}
import slick.jdbc.JdbcProfile
import tasks.{ProcessTargetConnector, TargetConnector, TaskConnector, TaskParser, TaskParserCharp}
import javax.inject.Inject
import org.tmatesoft.svn.core.SVNLogEntry
import play.api.db.slick.DatabaseConfigProvider
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class UpdateDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: Configuration)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  private def loadSvnRepository(repoConfName: String) = {
    lazy val suffix = RepoConf.suffix(repoConfName)
    lazy val taskConf = RepoConf.taskParser(repoConfName)
    lazy val scmConf = RepoConf.scm(repoConfName)

    implicit val parser: TaskParser = TaskParserCharp(taskConf.pattern, taskConf.split, taskConf.separator)

    lazy val rep = new SvnConnectorFactory {}
    lazy val repository: Future[SvnConnector] = rep.connect(scmConf.url, scmConf.user, scmConf.pass)

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

  def update(suffix: DatabaseSuffix, from: Option[Long], to: Option[Long]): Future[Seq[Int]] = {
    updateRepositoryRange(suffix.suffix, from.getOrElse(-1), to.getOrElse(-1))
  }

  def updateAll(): Future[Seq[Int]] = {
    val repSuffixes = conf.getOptional[Seq[String]]("repos").getOrElse(Seq())
    val repositories = repSuffixes.map(updateRepositoryAuto)
    Future.sequence(repositories).map(_.flatten)
  }

  def updateCustomFields(suffix: DatabaseSuffix, field: String, from: Option[Long], to: Option[Long]): Future[Seq[Int]] = {
    val repository = loadSvnRepository(suffix.suffix)
    repository.flatMap(rep => rep.updateCustomFields(field, from.getOrElse(-1), to.getOrElse(-1)))
  }

}
