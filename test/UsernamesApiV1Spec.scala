import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class UsernamesApiV1Spec extends ApiSpecification {
  "/api usernames" should {
    s"return a list of users" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/usernames")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"id":1,"name":"User1"},{"id":2,"name":"User2"},{"id":3,"name":"User3"}]""").ignoreSpace
    }
  }
}