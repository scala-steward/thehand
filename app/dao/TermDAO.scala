package dao

import java.sql.Date

import api.Api.Sorting.{ ASC, DESC }
import api.Page
import javax.inject.{ Inject, Singleton }
import models._
import java.time.LocalDate
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait TermComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  @Singleton
  class TermTable(tag: Tag) extends Table[Term](tag, "TERMS") {
    def phaseId = column[Long]("phase_id")
    def order = column[Long]("order")
    def text = column[String]("text")
    def date = column[LocalDate]("date")
    def deadline = column[Option[LocalDate]]("deadline")
    def done = column[Boolean]("done")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (phaseId, order, text, date, deadline, done, id) <> ((Term.apply _).tupled, Term.unapply)
  }
}

class TermDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends TermComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  val terms = TableQuery[TermTable]((tag: Tag) => new TermTable(tag))

  private def lastInPhase(phaseId: Long): Future[Long] = db.run {
    terms.filter(_.phaseId === phaseId)
      .map(_.id)
      .max.result
      .map(i => i.getOrElse(-1L))
  }

  def findById(id: Long): Future[Option[Term]] = db.run {
    terms.filter(_.id === id).result.headOption
  }

  def insert(phaseId: Long, text: String, date: LocalDate, deadline: Option[LocalDate]): Future[Int] = {
    lastInPhase(phaseId)
      .flatMap(max =>
        db.run(terms += Term(phaseId, order = max + 1L, text, date, deadline, done = false)))
  }

  def basicUpdate(id: Long, text: String, deadline: Option[LocalDate]): Future[Int] = db.run {
    for {
      u <- terms.filter(_.id === id).result.headOption
      i <- terms.update(u.get.copy(text = text, deadline = deadline)) if u.isDefined
    } yield i
  }

  private def updateDecrementOder(phaseId: Long, newOrder: Long, oldOrder: Long): Future[Seq[Future[Int]]] = {
    def getTerms: Future[Seq[Term]] = db.run {
      terms
        .filter(_.phaseId === phaseId)
        .filter(_.order > oldOrder)
        .filter(_.order <= newOrder)
        .result
    }
    getTerms.map(s => s.map(u => db.run { terms.update(u.copy(order = u.order - 1L)) }))
  }

  private def updateIncrementOder(phaseId: Long, newOrder: Long, oldOrder: Long): Future[Seq[Future[Int]]] = {
    def getTerms: Future[Seq[Term]] = db.run {
      terms
        .filter(_.phaseId === phaseId)
        .filter(_.order >= newOrder)
        .filter(_.order < oldOrder)
        .result
    }

    getTerms.map(s => s.map(u => db.run { terms.update(u.copy(order = u.order + 1L)) }))
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

  def newOrder(term: Term, order: Long): Future[Long] = {
    lastInPhase(term.phaseId)
      .map(max => Math.min(max, order))
      .map(min => Math.max(0, min))
      .map(newOrder => newOrder)
  }

  def updateOrder(id: Long, order: Long): Future[Int] = {
    findById(id)
      .flatMap(t => newOrder(t.get, order)
        .flatMap(n => updateOthers(t.get.phaseId, n, t.get.order)
          .flatMap(x => Future.sequence(x)
            .map(y => y.sum))))
  }

  def updatePhase(id: Long, phaseId: Long): Future[Int] = {
    for {
      _ <- updateOrder(id, Int.MaxValue)
      s <- findById(id)
      o <- lastInPhase(phaseId)
      i <- db.run { terms.update(s.get.copy(phaseId = phaseId, order = o + 1)) }
    } yield i
  }

  def updateDone(id: Long, done: Boolean): Future[Int] = db.run {
    for {
      u <- terms.filter(_.id === id).result.headOption
      i <- terms.update(u.get.copy(done = done)) if u.isDefined
    } yield i
  }

  def delete(id: Long): Future[Unit] =
    db.run(terms.filter(_.id === id).delete).map(_ => ())

  // List with all the available sorting fields.
  val sortingFields: Seq[String] = Seq("id", "order", "date", "deadline", "done")

  private def filterSort(phaseId: Long, done: Option[Boolean], fieldsWithOrder: (String, Boolean)): Future[Seq[Term]] = {
    val b = terms.filter(_.phaseId === phaseId).filter(_.done === done)
    fieldsWithOrder match {
      case ("id", ASC) => db.run { b.sortBy(_.id.asc).result }
      case ("id", DESC) => db.run { b.sortBy(_.id.desc).result }
      case ("order", ASC) => db.run { b.sortBy(_.order.asc).result }
      case ("order", DESC) => db.run { b.sortBy(_.order.desc).result }
      case ("date", ASC) => db.run { b.sortBy(_.date.asc).result }
      case ("date", DESC) => db.run { b.sortBy(_.date.desc).result }
      case ("deadline", ASC) => db.run { b.sortBy(_.deadline.asc).result }
      case ("deadline", DESC) => db.run { b.sortBy(_.deadline.asc).result }
      case ("done", ASC) => db.run { b.sortBy(_.done.asc).result }
      case ("done", DESC) => db.run { b.sortBy(_.done.desc).result }
      case _ => db.run { b.result }
    }
  }

  def createPage(phases: Seq[Term], p: Int, s: Int): Page[Term] = {
    Page(
      items = phases.slice((p - 1) * s, (p - 1) * s + s),
      page = p,
      size = s,
      total = phases.size.toLong)
  }

  def page(userId: Long, done: Option[Boolean], sortingFields: Seq[(String, Boolean)], p: Int, s: Int): Future[Page[Term]] = {
    filterSort(userId, done, sortingFields.head)
      .map(seq => createPage(seq, p, s))
  }

}