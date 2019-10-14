import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import tasks.ProcessTargetConnector
import models.{CustomFields, Task}

class ProcessTargetConnectorSpec extends Specification with Matchers  {
  val processor = ProcessTargetConnector(TaskConnectorFixture())

  s2"Get task with id 1 $e1"
  val someTask = Some(Task(Some("Bug"),Some(8),Some(13),Some(1.9),Some(12),1,0))
  def e1 = processor.process(1)  must beEqualTo(someTask)

  s2"Get custom field Request Type should return $e2"
  val someCustomField = Some(CustomFields(Some("Issue"),"Request Type",1,0))
  def e2 = processor.processCustomFields(1, "Request Type")  must beEqualTo(someCustomField)

  s2"Get custom field Work not should return $e3"
  def e3 = processor.processCustomFields(1, "Work")  must beNone
}
