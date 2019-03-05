package models

import java.time.LocalDate

import play.api.libs.json.{ Json, OFormat }

case class Term(
  phaseId: Long,
  order: Long,
  text: String,
  date: LocalDate,
  deadline: Option[LocalDate],
  done: Boolean,
  id: Long = 0L)

object Term {
  implicit val parserFormat: OFormat[Term] = Json.format[Term]
}