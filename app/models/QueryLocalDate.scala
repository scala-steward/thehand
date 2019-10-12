package models

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.mvc.PathBindable

import scala.util.{Failure, Success, Try}

case class QueryLocalDate(date: LocalDate)

object QueryLocalDate {
  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit def pathBinder(implicit intBinder: PathBindable[Int]): PathBindable[QueryLocalDate] = new PathBindable[QueryLocalDate] {
    override def bind(key: String, value: String): Either[String, QueryLocalDate] = {
      Try {
        QueryLocalDate(LocalDate.parse(value, dateFormatter))
      } match {
        case Success(s) => Right(s)
        case Failure(value) => Left(s"$value cannot be parsed as a date!")
      }
    }
    override def unbind(key: String, value: QueryLocalDate): String = {
      s"$key=${value.date.format(dateFormatter)}"
    }
  }
}