package dao

import java.sql.Timestamp

import javax.inject.Inject
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class ReportDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends AuthorComponent
  with CommitComponent
  with CommitEntryFileComponent
  with CommitTaskComponent
  with CustomFieldsComponent
  with TaskComponent
  with LocFileComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def filesBugsCounter(suffix: DatabaseSuffix): Future[Seq[(String, Int)]] = db.run {
    val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
    val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))

    val bugs = for {
      co <- commits
      cf <- commitsFiles if cf.revisionId === co.id
      ct <- commitTasks if ct.commitId === co.id
      tk <- tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
      fi <- files if fi.id === cf.pathId
    } yield fi
    val countBugs = bugs
      .groupBy(_.path)
      .map {
        case (path, group) =>
          (path, group.length)
      }
    countBugs.result.transactionally
  }

  def fileAuthorCommitsCounter(author: String, suffix: DatabaseSuffix): Future[Seq[(String, Int)]] = db.run {
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))

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

  def fileAuthorCommitsBugsCounter(author: String, suffix: DatabaseSuffix): Future[Seq[(String, Int)]] = db.run {
    val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
    val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))

    val cmts = for {
      ai <- authors if ai.author === author
      co <- commits if co.authorId === ai.id
      cf <- commitsFiles if cf.revisionId === co.id
      ct <- commitTasks if ct.commitId === co.id
      tk <- tasks if tk.taskId === ct.taskId && tk.typeTaskId === 8L //8L == BUG for target process
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

  def filterMovedFiles(revisionId: Long, suffix: DatabaseSuffix): Future[Seq[CommitEntryFile]] = db.run {
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    val files = for {
      cf <- commitsFiles if cf.revisionId === revisionId && cf.copyPathId >= 1L
    } yield cf
    files.result.transactionally
  }

  def authorsNames(suffix: DatabaseSuffix): Future[Seq[String]] = db.run {
    val authors = TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    authors.map(_.author).result
  }

//  def countCommitByCustomField(suffix: DatabaseSuffix, fieldValue: String, initialTime: Timestamp, finalTime: Timestamp) : Future[Seq[(String, Int)]] = db.run {
//    val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
//    val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
//    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
//    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
//    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
//    val customFields = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
//
//    def countCommitTask(field_value: String, initialTime: Timestamp, finalTime: Timestamp)= for {
//      cs <- customFields if cs.field_value === fieldValue
//      taskParents <- tasks if cs.taskId === taskParents.taskId
//      tsk <- tasks if tsk.taskId === taskParents.taskId || tsk.parentId === taskParents.taskId
//      co <- commits if co.timestamp >= initialTime && co.timestamp <= finalTime
//      ct <- commitTasks if ct.commitId === co.id && ct.taskId === tsk.taskId
//      cf <- commitsFiles if cf.revisionId === co.id
//      fi <- files if fi.id === cf.pathId
//    } yield fi
//    val countCommits = countCommitTask(fieldValue, initialTime, finalTime)
//      .groupBy(_.path)
//      .map {
//        case (path, group) =>
//          (path, group.length)
//      }.sortBy(_._2)
//    countCommits.result.transactionally
//  }

  def countCommitByCustomField(suffix: DatabaseSuffix, fieldValue: String, initialTime: Timestamp, finalTime: Timestamp) : Future[Seq[(String, Int)]] = db.run {
    val commitTasks = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))
    val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    val files = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val commits = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val commitsFiles = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    val customFields = TableQuery[CustomFieldsTable]((tag: Tag) => new CustomFieldsTable(tag, suffix))
    val locs = TableQuery[LocFilesTable]((tag: Tag) => new LocFilesTable(tag, suffix))

    def countCommitTask(field_value: String, initialTime: Timestamp, finalTime: Timestamp) = for {
      cs <- customFields if cs.field_value === fieldValue
      taskParents <- tasks if cs.taskId === taskParents.taskId
      tsk <- tasks if tsk.taskId === taskParents.taskId || tsk.parentId === taskParents.taskId
      co <- commits if co.timestamp >= initialTime && co.timestamp <= finalTime
      ct <- commitTasks if ct.commitId === co.id && ct.taskId === tsk.taskId
      cf <- commitsFiles if cf.revisionId === co.id
      fi <- files if fi.id === cf.pathId
      lc <- locs if lc.fileRef === fi.id
    } yield fi.path

    val countCommits = countCommitTask(fieldValue, initialTime, finalTime)
      .groupBy(a => a)
      .map(f => (f._1, f._2.length))
      .sortBy(_._2)

    countCommits.result.transactionally
  }

}
