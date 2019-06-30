import ApplicationFixture.fixture
import models.DatabeSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class CommitTasksApiV1Spec extends ApiSpecification {
  fixture.populate(DatabeSuffix("COMMITS_TASKS_"))
  "/api commit tastks" should {
    s"return a list of commit tasks" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMITS_TASKS_/committasks")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"taskId":1,"commitId":1,"id":1},
           {"taskId":2,"commitId":3,"id":2},
           {"taskId":3,"commitId":2,"id":3},
           {"taskId":5,"commitId":3,"id":4}]""").ignoreSpace
    }
    s"return a commit tasks by id" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMITS_TASKS_/committasks/3")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"taskId":3,"commitId":2,"id":3}]""").ignoreSpace
    }
  }
}
