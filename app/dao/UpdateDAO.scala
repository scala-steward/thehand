package dao

import conf.{RepoConf, TargetConf}
import models._
import scm.{ScmRepositoryData, SvnConnector, SvnConnectorFactory, SvnExtractor}
import tasks.{ProcessTargetConnector, TargetConnector, TaskConnector, TaskParser, TaskParserOctothorpe}
import javax.inject.Inject
import org.tmatesoft.svn.core.SVNLogEntry
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class UpdateDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  private def loadSvnRepository(repoConfName: String) = {
    lazy val suffix = RepoConf.suffix(repoConfName)
    lazy val taskConf = RepoConf.taskParser(repoConfName)
    lazy val scmConf = RepoConf.scm(repoConfName)

    implicit val parser: TaskParser = TaskParserOctothorpe(taskConf.pattern, taskConf.split, taskConf.separator)

    lazy val rep = new SvnConnectorFactory {}
    lazy val repository: Future[SvnConnector] = rep.connect(scmConf.url, scmConf.user, scmConf.pass)
    val taskConnector: TaskConnector = TargetConnector(TargetConf.auth.url, TargetConf.auth.user, TargetConf.auth.pass)

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
    val repositories = RepoConf.repos.map(updateRepositoryAuto)
    Future.sequence(repositories).map(_.flatten)
  }

  def updateCustomFields(suffix: DatabaseSuffix, field: String, from: Option[Long], to: Option[Long]): Future[Seq[Int]] = {
    val repository = loadSvnRepository(suffix.suffix)
    repository.flatMap(rep => rep.updateCustomFields(field, from.getOrElse(-1), to.getOrElse(-1)))
  }

}
