package models

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import play.api.mvc.PathBindable

case class QueryLocalDate(date: LocalDate)

object QueryLocalDate {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit def pathBinder(implicit intBinder: PathBindable[Int]) = new PathBindable[QueryLocalDate] {
    override def bind(key: String, value: String): Either[String, QueryLocalDate] = {
        try {
          Right(QueryLocalDate(LocalDate.parse(value, dateFormatter)))
        } catch {
          case _: DateTimeParseException => Left(s"$value cannot be parsed as a date!")
        }
    }
    override def unbind(key: String, value: QueryLocalDate): String = {
      s"$key=${value.date.format(dateFormatter)}"
    }
  }
}