package conf

import com.typesafe.config.ConfigFactory
import models.DatabaseSuffix
import play.api.Configuration

case class TaskConf(pattern: String, split: String, separator: String)


object RepoConf {
  private lazy val conf = new Configuration(ConfigFactory.load())

  def taskParser(repo: String): TaskConf = {
    TaskConf(conf.get[String](repo + ".task_model.patternParser"),
    conf.get[String](repo + ".task_model.patternSplit"),
    conf.get[String](repo + ".task_model.separator"))
  }

  def scm(repo: String): BasicAuthConf = {
    BasicAuthConf(conf.get[String](repo + ".url"),
    conf.get[String](repo + ".user"),
    conf.get[String](repo + ".pass"))
  }

  def suffix(repo: String): DatabaseSuffix = {
    DatabaseSuffix(conf.get[String](repo + ".database_suffix"))
  }

  def repos(): Seq[String] = {
    conf.getOptional[Seq[String]]("repos").getOrElse(Seq())
  }
}
