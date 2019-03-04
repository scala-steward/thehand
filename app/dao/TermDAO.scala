package dao

import java.sql.Date

import api.Api.Sorting.{ ASC, DESC }
import javax.inject.Inject
import models._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

trait TermComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class TermTable(tag: Tag) extends Table[Term](tag, "TERMS") {
    def phaseId = column[Long]("term_id")
    def order = column[Long]("order")
    def text = column[String]("text")
    def date = column[Date]("date")
    def deadline = column[Option[Date]]("deadline")
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

  def insert(phaseId: Long, text: String, date: Date, deadline: Option[Date]) = {
    lastInPhase(phaseId)
      .flatMap(max =>
        db.run(terms += Term(phaseId, order = max + 1L, text, date, deadline, done = false)))
  }

  def basicUpdate(id: Long, text: String, deadline: Option[Date]): Future[Int] = db.run {
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

  def newOrder(term: Term, order: Int): Future[Long] = {
    lastInPhase(term.phaseId)
      .map(max => Math.min(max, order))
      .map(min => Math.max(0, min))
      .map(newOrder => newOrder)
  }

  def updateOrder(id: Long, order: Int): Future[Int] = {
    findById(id)
      .flatMap(t => newOrder(t.get, order)
        .flatMap(n => updateOthers(t.get.phaseId, n, t.get.order)
          .flatMap(x => Future.sequence(x)
            .map(y => y.sum))))
  }

  def updatePhase(id: Long, phaseId: Long) = {
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
  val sortingFields = Seq("id", "order", "date", "deadline", "done")
  // Defines a sorting function for the pair (field, order)
  def sortingFunc(fieldsWithOrder: (String, Boolean)): (Term, Term) => Boolean = fieldsWithOrder match {
    case ("id", ASC) => _.id < _.id
    case ("id", DESC) => _.id > _.id
    case ("order", ASC) => _.order < _.order
    case ("order", DESC) => _.order > _.order
    case ("date", ASC) => _.date before _.date
    case ("date", DESC) => _.date after _.date
    case ("deadline", ASC) => (a, b) => a.deadline.exists(ad => b.deadline.forall(bd => ad before bd))
    case ("deadline", DESC) => (a, b) => a.deadline.exists(ad => b.deadline.forall(bd => ad after bd))
    case ("done", ASC) => _.done > _.done
    case ("done", DESC) => _.done < _.done
    case _ => (_, _) => false
  }

}