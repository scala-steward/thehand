import java.time.LocalDate
import java.time.format.DateTimeFormatter

import dao._
import javax.inject.Inject
import models.{ApiKey, Author, CommitEntry, CommitEntryFile, CommitTasks, EntryFile, Phase, Suffix, Task, Term, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, ExecutionContext, Future}

class DatabaseFixture @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends AuthorComponent
  with CommitComponent
  with CommitEntryFileComponent
  with CommitTaskComponent
  with TaskComponent
  with PersonComponent
  with ApiKeyComponent
  with ApiTokenComponent
  with ApiLogComponent
  with PhaseComponent
  with TermComponent
  with UserComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  @throws
  private def parseDateTime(s: String): LocalDate = {
    java.time.LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }

  private implicit val apiKeys: Seq[ApiKey] = Seq(
    ApiKey(apiKey = "AbCdEfGhIjK1", name = "ios-app", active = true),
    ApiKey(apiKey = "AbCdEfGhIjK2", name = "android-app", active = true))

  private implicit val users: Seq[User] = Seq(
    User("user1@mail.com", "123456", "User 1", emailConfirmed = true, active = true, 1L),
    User("user2@mail.com", "123456", "User 2", emailConfirmed = true, active = true, 2L),
    User("user3@mail.com", "123456", "User 3", emailConfirmed = true, active = true, 3L))

  private implicit val phases: Seq[Phase] = Seq(
    Phase(1L, 0, "Personal", 1L),
    Phase(1L, 1, "Work", 2L),
    Phase(1L, 2, "Home", 3L))

  private implicit val terms: Seq[Term] = Seq(
    Term(1L, 0, "Shirts on dry cleaner", parseDateTime("2015-09-06T10:11:00"), Some(parseDateTime("2015-09-08T17:00:00")), done = true, 1L),
    Term(1L, 1, "Gift for Mum's birthday", parseDateTime("2015-09-05T12:24:32"), Some(parseDateTime("2015-10-22T00:00:00")), done = false, 2L),
    Term(1L, 2, "Plan the Barcelona's trip", parseDateTime("2015-09-06T14:41:11"), None, done = false, 3L),
    Term(2L, 0, "Check monday's report", parseDateTime("2015-09-06T09:21:00"), Some(parseDateTime("2015-09-08T18:00:00")), done = false, 4L),
    Term(2L, 1, "Call conference with Jonh", parseDateTime("2015-09-06T11:37:00"), Some(parseDateTime("2015-09-07T18:00:00")), done = false, 5L),
    Term(3L, 0, "Fix the lamp", parseDateTime("2015-08-16T21:22:00"), None, done = false, 6L),
    Term(3L, 1, "Buy coffee", parseDateTime("2015-09-05T08:12:00"), None, done = false, 7L))

  def populate(): Future[Unit] = {
    val apiKey = TableQuery[ApiKeyTable]((tag: Tag) => new ApiKeyTable(tag))
    val phase = TableQuery[PhaseTable]((tag: Tag) => new PhaseTable(tag))
    val term = TableQuery[TermTable]((tag: Tag) => new TermTable(tag))
    val user = TableQuery[UserTable]((tag: Tag) => new UserTable(tag))

    db.run((apiKey ++= apiKeys).asTry andThen
      (user ++= users).asTry andThen
      (phase ++= phases).asTry andFinally
      (term ++= terms).asTry).map(_ => ())
  }

  private val tasks = Seq(
    Task(Some("Task"), Some(5L), Some(20), None, 1),
    Task(Some("Task"), Some(5L), Some(20), None, 2),
    Task(Some("Bug"), Some(8L), Some(20), Some(1), 3),
    Task(Some("Bug"), Some(8L), Some(20), Some(2), 4),
    Task(Some("Bug"), Some(8L), Some(20), Some(2), 5))

  private val authors =
    Seq(Author("john"), Author("philips"), Author("thomas"))

  private val commits = Seq(
    CommitEntry(Some("Task #1"), None, 1, 0, 1),
    CommitEntry(Some("Task #2"), None, 2, 0, 2),
    CommitEntry(Some("Bug #3"), None, 2, 0, 3),
    CommitEntry(Some("Bug #4"), None, 2, 0, 4),
    CommitEntry(Some("Bug #5"), None, 3, 0, 5))

  private val files = Seq(
    EntryFile("/zip"),
    EntryFile("/zap"),
    EntryFile("/zop"),
    EntryFile("/zip"))

  private val comitFiles = Seq(
    CommitEntryFile(Some('A'), None, Some(1), 0, 0),
    CommitEntryFile(Some('A'), None, Some(1), 0, 0),
    CommitEntryFile(Some('A'), None, Some(1), 0, 0),
    CommitEntryFile(Some('M'), None, Some(2), 0, 0),
    CommitEntryFile(Some('M'), None, Some(3), 0, 0),
    CommitEntryFile(Some('M'), None, Some(4), 0, 0),
    CommitEntryFile(Some('D'), None, Some(5), 0, 0))

  private val commitsTasks = Seq(
    CommitTasks(1, 1, 1),
    CommitTasks(2, 3, 2),
    CommitTasks(3, 2, 3),
    CommitTasks(5, 3))

  def populate(suffix: Suffix): Future[Unit] = {
    val task = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    val author =TableQuery[AuthorsTable]((tag: Tag) => new AuthorsTable(tag, suffix))
    val commit = TableQuery[CommitTable]((tag: Tag) => new CommitTable(tag, suffix))
    val file = TableQuery[EntryFilesTable]((tag: Tag) => new EntryFilesTable(tag, suffix))
    val commitFile = TableQuery[CommitEntryFileTable]((tag: Tag) => new CommitEntryFileTable(tag, suffix))
    val commitTask = TableQuery[CommitTasksTable]((tag: Tag) => new CommitTasksTable(tag, suffix))

    db.run((task ++= tasks).asTry andThen
      (author ++= authors).asTry andThen
      (commit ++= commits).asTry andThen
      (file ++= files).asTry andThen
      (commitFile ++= comitFiles).asTry andThen
      (commitTask ++= commitsTasks).asTry).map(_ => ())
  }

}
