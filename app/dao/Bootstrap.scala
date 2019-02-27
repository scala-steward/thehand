package dao

import scala.concurrent.duration._
import scala.language.postfixOps

import javax.inject.{Inject, Singleton}
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import thehand.telemetrics.HandLogger

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton()
class Bootstrap @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext, suffix: Suffix)
  extends AuthorComponent
    with CommitComponent
    with CommitEntryFileComponent
    with CommitTaskComponent
    with TaskComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
  private val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
  private val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
  private val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
  private val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
  private val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))

  def createSchemas(): Unit = {
    exec(
      tasks.schema.create.asTry andThen
      authors.schema.create.asTry andThen
      commits.schema.create.asTry andThen
      files.schema.create.asTry andThen
      commitsFiles.schema.create.asTry andThen
      commitTasks.schema.create.asTry) onComplete {
      case Success(_) => HandLogger.debug("correct create tables")
      case Failure(e) =>
        HandLogger.error("error in create tables " + e.getMessage)
    }
  }

  private def exec[T](program: DBIO[T]): Future[T] =
    Await.ready(db.run(program), 2 minutes)
}
