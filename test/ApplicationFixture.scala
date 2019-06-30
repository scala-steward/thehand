import dao.Boot
import models.Suffix
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

object ApplicationFixture {
  lazy val app: Application = new GuiceApplicationBuilder().
    configure(
      "slick.dbs.mydb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.mydb.db.driver" -> "org.h2.Driver",
      "slick.dbs.mydb.db.url" -> "jdbc:h2:mem:blah;",
      "slick.dbs.mydb.db.user" -> "test",
      "slick.dbs.mydb.db.password" -> "").build

  private val daoBootstrap: Boot = Application.instanceCache[Boot].apply(app)
  lazy val fixture: DatabaseFixture = Application.instanceCache[DatabaseFixture].apply(app)

  def initializeWithData() = {
    daoBootstrap.createSchemas()
    fixture.populate()
  }

  def initializeWithData(suffix: Suffix) = {
    daoBootstrap.createSchemas(suffix)
    fixture.populate(suffix)
  }
}
