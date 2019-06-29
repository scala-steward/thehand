import dao.{AuthorDAO, BootstrapDAO, CommitDAO, CommitEntryFileDAO, CommitTaskDAO, EntryFileDAO, TaskDAO}
 import models.Suffix
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext}

class DatabaseSuffixFixture {
  implicit val context = ExecutionContext.Implicits.global
  private val app: Application = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.mydb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.mydb.db.driver" -> "org.h2.Driver",
      "slick.dbs.mydb.db.url" -> "jdbc:h2:mem:blah;",
      "slick.dbs.mydb.db.user" -> "test",
      "slick.dbs.mydb.db.password" -> "").build

  def daoTasks: TaskDAO = Application.instanceCache[TaskDAO].apply(app)
  def daoAuthors: AuthorDAO = Application.instanceCache[AuthorDAO].apply(app)
  def daoCommits: CommitDAO = Application.instanceCache[CommitDAO].apply(app)
  def daoFiles: EntryFileDAO = Application.instanceCache[EntryFileDAO].apply(app)
  def daoCommitFiles: CommitEntryFileDAO = Application.instanceCache[CommitEntryFileDAO].apply(app)
  def daoCommitTasks: CommitTaskDAO = Application.instanceCache[CommitTaskDAO].apply(app)

  def populate(suffix: Suffix): Seq[Int] = {
    val daoBootstrap: BootstrapDAO = Application.instanceCache[BootstrapDAO].apply(app)
    daoBootstrap.createSchemas(suffix)
    val insertAll = for {
      _ <- daoTasks.insert(ExtractorFixture.extractTasks, suffix)
      _ <- daoAuthors.insert(ExtractorFixture.extractAuthors, suffix)
      _ <- daoCommits.insert(ExtractorFixture.extractCommits, suffix)
      _ <- daoFiles.insert(ExtractorFixture.extractFiles, suffix)
      _ <- daoCommitTasks.insert(ExtractorFixture.extractCommitsTasks, suffix)
      c <- daoCommitFiles.insert(ExtractorFixture.extractCommitsFiles, suffix)
    } yield c
    Await.result(insertAll, 2 seconds)
  }
}
