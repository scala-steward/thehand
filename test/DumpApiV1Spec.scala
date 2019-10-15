import ApplicationFixture.fixture
import models.DatabaseSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class DumpApiV1Spec extends ApiSpecification {
  fixture.populate(DatabaseSuffix("DUMP_"))
  "/api author" should {
    s"return a list of files" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/DUMP_/dump/2010-10-10/to/2018-10-10")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[[1,"john",1,"Task#1",1441545060000,1,"/zip",65,1,"Task",null,20],
           |[1,"john",1,"Task#1",1441545060000,2,"/zap",65,1,"Task",null,20],
           |[1,"john",1,"Task#1",1441545060000,3,"/zop",65,1,"Task",null,20],
           |[2,"philips",2,"Bug#4",1449317460000,1,"/zip",77,3,"Bug",null,20],
           |[2,"philips",2,"Bug#4",1449317460000,2,"/zap",77,3,"Bug",null,20],
           |[3,"thomas",3,"Bug#5",1452082260000,1,"/zip",68,2,"Task",null,20],
           |[3,"thomas",3,"Bug#5",1452082260000,1,"/zip",68,5,"Bug",null,20]]""".stripMargin).ignoreSpace
    }
  }
}