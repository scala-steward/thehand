
package models

import play.api.mvc.PathBindable

case class DatabaseSuffix(suffix: String)

object DatabaseSuffix {
  implicit def pathBinder(implicit intBinder: PathBindable[String]) = new PathBindable[DatabaseSuffix] {
    override def bind(key: String, value: String): Either[String, DatabaseSuffix] = {
      val data = intBinder.bind(key, value)
      data match {
        case Right(suffix) if !suffix.trim.isEmpty => Right(DatabaseSuffix(suffix.trim))
        case Left(_) => Left("Unable to bind DatabaseSuffix")
      }
    }
    override def unbind(key: String, value: DatabaseSuffix): String = {
      value.suffix
    }
  }
}

