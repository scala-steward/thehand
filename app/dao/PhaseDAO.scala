package dao

import java.sql.Date

import api.Api.Sorting.{ ASC, DESC }
import api.Page
import javax.inject.{ Inject, Singleton }
import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait PhaseComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  @Singleton
  class PhaseTable(tag: Tag) extends Table[Phase](tag, "PHASES") {
    def userId = column[Long]("user_id")
    def order = column[Long]("order")
    def name = column[String]("text")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (userId, order, name, id) <> ((Phase.apply _).tupled, Phase.unapply)
  }
}

class PhaseDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends PhaseComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  val phases = TableQuery[PhaseTable]((tag: Tag) => new PhaseTable(tag))

  private def last(userId: Long): Future[Long] = db.run {
    phases.filter(_.userId === userId)
      .map(_.order)
      .max.result
      .map(i => i.getOrElse(-1L))
  }

  def findById(id: Long): Future[Option[Phase]] = db.run {
    phases.filter(_.id === id).result.headOption
  }

  def insert(userId: Long, name: String): Future[Int] = {
    last(userId)
      .flatMap(max =>
        db.run(phases += Phase(userId, order = max + 1L, name)))
  }

  def basicUpdate(id: Long, name: String): Future[Int] = db.run {
    for {
      u <- phases.filter(_.id === id).result.headOption
      i <- phases.update(u.get.copy(name = name)) if u.isDefined
    } yield i
  }

  private def updateDecrementOder(userId: Long, newOrder: Long, oldOrder: Long): Future[Seq[Future[Int]]] = {
    def getTerms: Future[Seq[Phase]] = db.run {
      phases
        .filter(_.userId === userId)
        .filter(_.order > oldOrder)
        .filter(_.order <= newOrder)
        .result
    }
    getTerms.map(s => s.map(u => db.run { phases.update(u.copy(order = u.order - 1L)) }))
  }

  private def updateIncrementOder(userId: Long, newOrder: Long, oldOrder: Long): Future[Seq[Future[Int]]] = {
    def getTerms: Future[Seq[Phase]] = db.run {
      phases
        .filter(_.userId === userId)
        .filter(_.order >= newOrder)
        .filter(_.order < oldOrder)
        .result
    }

    getTerms.map(s => s.map(u => db.run { phases.update(u.copy(order = u.order + 1L)) }))
  }

  def updateOthers(phaseId: Long, newOrder: Long, oldOrder: Long): Future[Seq[Future[Int]]] = {
    if (newOrder > oldOrder) {
      updateDecrementOder(phaseId, newOrder, oldOrder)
    } else if (newOrder < oldOrder) {
      updateIncrementOder(phaseId, newOrder, oldOrder)
    } else {
      Future.failed(new Exception)
    }
  }

  def newOrder(phase: Phase, order: Int): Future[Long] = {
    last(phase.userId)
      .map(max => Math.min(max, order))
      .map(min => Math.max(0, min))
      .map(newOrder => newOrder)
  }

  def updateOrder(id: Long, order: Int): Future[Int] = {
    findById(id)
      .flatMap(t => newOrder(t.get, order)
        .flatMap(n => updateOthers(t.get.userId, n, t.get.order)
          .flatMap(x => Future.sequence(x)
            .map(y => y.sum))))
  }

  def delete(id: Long): Future[Unit] =
    db.run(phases.filter(_.id === id).delete).map(_ => ())

  private def filterSort(userId: Long, fieldsWithOrder: (String, Boolean)): Future[Seq[Phase]] = fieldsWithOrder match {
    case ("id", ASC) => db.run { phases.filter(_.userId === userId).sortBy(_.id.asc).result }
    case ("id", DESC) => db.run { phases.filter(_.userId === userId).sortBy(_.id.desc).result }
    case ("order", ASC) => db.run { phases.filter(_.userId === userId).sortBy(_.order.asc).result }
    case ("order", DESC) => db.run { phases.filter(_.userId === userId).sortBy(_.order.desc).result }
    case ("name", ASC) => db.run { phases.filter(_.userId === userId).sortBy(_.name.asc).result }
    case ("name", DESC) => db.run { phases.filter(_.userId === userId).sortBy(_.name.desc).result }
    case _ => db.run { phases.filter(_.userId === userId).result }
  }

  def createPage(phases: Seq[Phase], p: Int, s: Int): Page[Phase] = {
    Page(
      items = phases.slice((p - 1) * s, (p - 1) * s + s),
      page = p,
      size = s,
      total = phases.size)
  }

  def page(userId: Long, sortingFields: Seq[(String, Boolean)], p: Int, s: Int) = {
    filterSort(userId, sortingFields.head)
      .map(seq => createPage(seq, p, s))
  }

  // List with all the available sorting fields.
  val sortingFields = Seq("id", "order", "name")
}