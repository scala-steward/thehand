import ApplicationFixture.fixture
import models.DatabaseSuffix
import org.specs2.matcher._
import play.api.libs.json.Json
import play.api.mvc.Result

import scala.concurrent.Future

class BootApiV1Spec extends ApiSpecification {
  fixture.populate(DatabaseSuffix("BOOT_"))
  "/api boot" should {
    s"return accepted a boot command" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot/YOUR_MAGIC_SECRET", Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return accepted a boot command with table suffix" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot/BOOT_/YOUR_MAGIC_SECRET", Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return not found if call with magic a boot command" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot/", Json.obj())
      status(result) must equalTo(NOT_FOUND)
    }
    s"return not found if call with magic a boot command with table suffix" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot/BOOT_/", Json.obj())
      status(result) must equalTo(NOT_FOUND)
    }
  }

}