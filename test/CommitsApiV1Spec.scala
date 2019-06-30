import ApplicationFixture.fixture
import models.DatabeSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class CommitsApiV1Spec extends ApiSpecification {
  fixture.populate(DatabeSuffix("COMMITS_"))
  "/api commits" should {
    s"return a list of commits" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMITS_/commits")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"message":"Task#1","revision":1,"authorId":1,"id":1},
           |{"message":"Bug#4","revision":2,"authorId":2,"id":2},
           |{"message":"Bug#5","revision":3,"authorId":3,"id":3}]""".stripMargin).ignoreSpace
    }
    s"return a commit by id" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMITS_/commits/3")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[
             {
               "message": "Bug #5",
               "revision": 3,
               "authorId": 3,
               "id": 3
             }
           ]""").ignoreSpace
    }
  }
}
