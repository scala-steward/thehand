package tasks

import models.CustomFields

trait TaskProcessConnector {
  def process(id: Long, field: String): Option[TaskWithCustom]
  def processRange(ids: Seq[Long], field: String): Seq[TaskWithCustom]
  def processCustomFields(id: Long, field: String): Option[CustomFields]
}
