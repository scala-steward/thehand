import org.specs2.matcher._
import play.api.mvc.Result

import scala.concurrent.Future
import play.api.libs.json.Json

class UpdateApiV1Spec extends ApiSpecification {

  "/api update" should {
    s"return accepted a update command" in new Scope {
      val result: Future[Result] = routePOST(
        "/api/v1/update",Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return accepted a update demo table command" in new Scope {
      val result: Future[Result] = routePOST(
        "/api/v1/demo_/update",Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return exception on try update non existent table" in new Scope {
      val result = routePOST("/api/v1/unknow_/update",Json.obj())
      throwA[Exception](message = s"(error in read conf file!)")
    }
    s"return accepted a update demo custom field table" in new Scope {
      val result: Future[Result] = routePOST(
        "/api/v1/demo_/update/custom/field",Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return exception a update non existent custom field table" in new Scope {
      val result: Future[Result] = routePOST(
        "/api/v1/unknow_/update/custom/field",Json.obj())
      throwA[Exception](message = s"(error in read conf file!)")
    }
  }

}
