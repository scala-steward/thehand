package models

import play.api.libs.json._

final case class Person(username: String, name: String, age: Int, id: Long = 0L)

object Person {  
  implicit val personFormat: OFormat[Person] = Json.format[Person]
}
