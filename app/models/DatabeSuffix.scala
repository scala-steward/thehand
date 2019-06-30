
package models

import play.api.mvc.PathBindable

case class DatabeSuffix(suffix: String)

object DatabeSuffix {
  implicit def pathBinder(implicit intBinder: PathBindable[String]) = new PathBindable[DatabeSuffix] {
    override def bind(key: String, value: String): Either[String, DatabeSuffix] = {
      val data = intBinder.bind(key, value)
      data match {
        case Right(suffix) if !suffix.trim.isEmpty => Right(DatabeSuffix(suffix.trim))
        case Left(_) => Left("Unable to bind DatabaseSuffix")
      }
    }
    override def unbind(key: String, value: DatabeSuffix): String = {
      value.suffix
    }
  }
}

