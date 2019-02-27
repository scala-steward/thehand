package dao

import javax.inject.{ Inject, Singleton }

import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton()
class ReportDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit suffix: Suffix)
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

  def filesBugsCounter: Future[Seq[(String, Int)]] = db.run {
    val bugs = for {
      co <- commits
      cf <- commitsFiles if cf.revisionId === co.id
      fi <- files if fi.id === cf.pathId
      ct <- commitTasks if ct.commitId === co.id
      tk <- tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
    } yield fi
    val countBugs = bugs
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    countBugs.result.transactionally
  }

  def fileAuthorCommitsCounter(author: String): Future[Seq[(String, Int)]] = db.run {
    val cmts = for {
      ai <- authors if ai.author === author
      co <- commits if co.authorId === ai.id
      cf <- commitsFiles if cf.revisionId === co.id
      fi <- files if fi.id === cf.pathId
    } yield fi
    val countCommits = cmts
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    countCommits.result.transactionally
  }

  def fileAuthorCommitsBugsCounter(author: String): Future[Seq[(String, Int)]] = db.run {
    val cmts = for {
      ai <- authors if ai.author === author
      co <- commits if co.authorId === ai.id
      cf <- commitsFiles if cf.revisionId === co.id
      fi <- files if fi.id === cf.pathId
      ct <- commitTasks if ct.commitId === co.id
      tk <- tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
    } yield fi
    val countCommits = cmts
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    countCommits.result.transactionally
  }

  def filterMovedFiles(revisionId: Long): Future[Seq[CommitEntryFile]] = db.run {
    val files = for {
      cf <- commitsFiles if cf.revisionId === revisionId && cf.copyPathId >= 1L
    } yield cf
    files.result.transactionally
  }

  def authorsNames: Future[Seq[String]] = db.run {
    authors.map(_.author).result
  }
}
