import dao._
import javax.inject.Inject
import models.{ApiKey, DatabaseSuffix, User}
import play.api.Application
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class DatabaseFixture @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit executionContext: ExecutionContext,app: Application)
  extends ApiKeyComponent
  with ApiTokenComponent
  with ApiLogComponent
  with UserComponent
  with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private implicit val apiKeys: Seq[ApiKey] = Seq(
    ApiKey(apiKey = "AbCdEfGhIjK1", name = "ios-app", active = true),
    ApiKey(apiKey = "AbCdEfGhIjK2", name = "android-app", active = true))

  private implicit val users: Seq[User] = Seq(
    User("user1@mail.com", "123456", "User 1", emailConfirmed = true, active = true, 1L),
    User("user2@mail.com", "123456", "User 2", emailConfirmed = true, active = true, 2L),
    User("user3@mail.com", "123456", "User 3", emailConfirmed = true, active = true, 3L))

  def populate(): Unit = {
    val apiKey = TableQuery[ApiKeyTable]((tag: Tag) => new ApiKeyTable(tag))
    val user = TableQuery[UserTable]((tag: Tag) => new UserTable(tag))

    Await.result(db.run((apiKey ++= apiKeys).asTry andThen
      (user ++= users).asTry).map(_ => ()), 2 seconds)
  }

  val daoTasks: TaskDAO = Application.instanceCache[TaskDAO].apply(app)
  val daoAuthors: AuthorDAO = Application.instanceCache[AuthorDAO].apply(app)
  val daoCommits: CommitDAO = Application.instanceCache[CommitDAO].apply(app)
  val daoFiles: EntryFileDAO = Application.instanceCache[EntryFileDAO].apply(app)
  val daoCommitFiles: CommitEntryFileDAO = Application.instanceCache[CommitEntryFileDAO].apply(app)
  val daoCommitTasks: CommitTaskDAO = Application.instanceCache[CommitTaskDAO].apply(app)
  val daoCustomFields: CustomFieldsDAO = Application.instanceCache[CustomFieldsDAO].apply(app)
  val daoLineCounter: LocDAO = Application.instanceCache[LocDAO].apply(app)

  def populate(suffix: DatabaseSuffix) = {
    val daoBootstrap: BootDAO = Application.instanceCache[BootDAO].apply(app)
    daoBootstrap.createSchemas(suffix)
    val insertAll = for {
      _ <- daoTasks.insert(ExtractorFixture.extractTasks, suffix)
      _ <- daoCustomFields.insert(ExtractorFixture.customFields, suffix)
      _ <- daoAuthors.insert(ExtractorFixture.extractAuthors, suffix)
      _ <- daoCommits.insert(ExtractorFixture.extractCommits, suffix)
      _ <- daoFiles.insert(ExtractorFixture.extractFiles, suffix)
      _ <- daoLineCounter.insert(ExtractorFixture.lineCounterFiles, suffix)
      _ <- daoCommitTasks.insert(ExtractorFixture.extractCommitsTasks, suffix)
      c <- daoCommitFiles.insert(ExtractorFixture.extractCommitsFiles, suffix)
    } yield c
    Await.result(insertAll, 2 seconds)
  }

}
