import ApplicationFixture.fixture
import models.DatabaseSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class DumpApiV1Spec extends ApiSpecification {
  fixture.populate(DatabaseSuffix("DUMP_"))
  "/api dump" should {
    s"return a list of files" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/DUMP_/dump/2010-10-10/to/2018-10-10")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s.size must beEqualTo(466)
    }
    s"return a list of files" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/DUMP_/dump/2010-10-10/to/2018-10-10/csv")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "text/plain")
      val s = contentAsString(result)
      s.size must beEqualTo(520)
    }
  }
}