package models

import java.sql.Date

import play.api.libs.json.{Json, OFormat}

case class Period(periodId: Long,
                   order: Int,
                   text: String,
                   date: Date,
                   deadline: Option[Date],
                   done: Boolean,
                   id: Long = 0L)

object Period {
  implicit val parserFormat: OFormat[Period] = Json.format[Period]
}