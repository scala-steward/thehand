package tasks

import models.{CustomFields, Task}

trait TaskProcessConnector {
  def process(id: Long): Option[Task]
  def processCustomFields(id: Long, field: String): Option[CustomFields]
}
