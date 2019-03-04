package models

import java.sql.Date

import play.api.libs.json.{ Json, OFormat }

case class Term(
  phaseId: Long,
  order: Long,
  text: String,
  date: Date,
  deadline: Option[Date],
  done: Boolean,
  id: Long = 0L)

object Term {
  implicit val parserFormat: OFormat[Term] = Json.format[Term]
}