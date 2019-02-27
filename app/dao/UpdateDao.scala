package dao

import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import cross.{ScmConnector, SvnConnectorFactory, SvnRepositoryData}
import org.tmatesoft.svn.core.SVNLogEntry
import slick.jdbc.JdbcProfile
import thehand.telemetrics.HandLogger
import thehand.{TaskParser, TaskParserCharp}
import thehand.tasks.{TargetConnector, TaskConnector}

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

class UpdateDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, conf: play.api.Configuration) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def update(repositories: Seq[String]): Unit = {
    lazy val target: TaskConnector = TargetConnector(
      conf.getString("target.url").get,
      conf.getString("target.user").get,
      conf.getString("target.pass").get)

    def loadSvnRepository(task: TaskConnector, confName: String): Option[SvnRepositoryData] = {
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

      repository match {
        case Some(r) => Some(new SvnRepositoryData(dbConfigProvider, task, r, parser, suffix))
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

  def updateAll(): Unit = {
    lazy val repositories = Seq(
      "repository_eberick",
      "repository_qibulder",
      "repository_qi4d"
    )
    update(repositories)
  }

}
