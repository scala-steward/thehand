package dao

import conf.BootConf

import scala.concurrent.duration._
//import scala.language.postfixOps
import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import telemetrics.HandLogger

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BootDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AuthorComponent
  with CommitComponent
  with CommitEntryFileComponent
  with CommitTaskComponent
  with ApiKeyComponent
  with ApiTokenComponent
  with ApiLogComponent
  with UserComponent
  with CustomFieldsComponent
  with TaskComponent
  with LocFileComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def createSchemas(): Unit = {
    val apiLog = TableQuery[ApiLogTable]((tag: Tag) => new ApiLogTable(tag))
    val apiToken = TableQuery[ApiTokenTable]((tag: Tag) => new ApiTokenTable(tag))
    val apiKey = TableQuery[ApiKeyTable]((tag: Tag) => new ApiKeyTable(tag))
    val user = TableQuery[UserTable]((tag: Tag) => new UserTable(tag))

    exec(apiLog.schema.create.asTry andThen
      apiToken.schema.create.asTry andThen
      apiKey.schema.create.asTry andThen
      user.schema.create.asTry) onComplete {
      case Success(_) => HandLogger.debug("correct create default tables")
      case Failure(e) =>
        HandLogger.error("error in create tables " + e.getMessage)
    }
  }

  def createSchemas(suffix: DatabaseSuffix): Unit = {
    val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
    val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    val customFields = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    val filesLoc = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))

    exec(
      tasks.schema.create.asTry andThen
        authors.schema.create.asTry andThen
        commits.schema.create.asTry andThen
        files.schema.create.asTry andThen
        commitsFiles.schema.create.asTry andThen
        commitTasks.schema.create.asTry andThen
        customFields.schema.create.asTry andThen
        filesLoc.schema.create.asTry) onComplete {
        case Success(_) => HandLogger.debug("correct create tables")
        case Failure(e) =>
          HandLogger.error("error in create tables " + e.getMessage)
      }
  }

  def createFirstApiKey() : Unit = {
    val apiKey = TableQuery[ApiKeyTable]((tag: Tag) => new ApiKeyTable(tag))
    val apiKeys: Seq[ApiKey] = Seq(
      ApiKey(apiKey = BootConf.first_api_key, name = "first-app", active = true))

    exec(apiKey ++= apiKeys) onComplete {
      case Success(_) => HandLogger.debug("correct create default tables")
      case Failure(e) => HandLogger.error("error in insert first api key " + e.getMessage)
    }
  }

  private def exec[T](program: DBIO[T]): Future[T] =
    Await.ready(db.run(program), 2 minutes)
}
