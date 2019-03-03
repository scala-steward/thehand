package dao

import javax.inject.{ Inject, Singleton }
import models.Person
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait PersonComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class PeopleTable(tag: Tag) extends Table[Person](tag, "people") {
    def username = column[String]("username", O.Unique)
    def name = column[String]("name")
    def age = column[Int]("age")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (username, name, age, id) <> ((Person.apply _).tupled, Person.unapply)
  }
}

@Singleton
class PersonDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends PersonComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val people = TableQuery[PeopleTable]

  //  def create_(username: String, name: String, age: Int) = db.run {
  //    val person = Person(username, name, age)
  //    (people returning people.map(_.id)
  //      into ((person, newId) => person.copy(id = newId))
  //      ) += person
  //  }

  def create(username: String, name: String, age: Int) = db.run {
    people += Person(username, name, age)
  }

  def update(id: Int, username: String, name: String, age: Int) = db.run {
    def upsert(personId: Option[Long]) = {
      if (personId.isEmpty) people += Person(username, name, age, 0)
      else people.insertOrUpdate(Person(username, name, age, personId.head))
    }

    for {
      personId <- people.filter(_.username === username).map(_.id).result.headOption
      u <- upsert(personId)
    } yield u
  }

  def list(): Future[Seq[Person]] = db.run {
    people.result
  }
}