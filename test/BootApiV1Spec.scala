import ApplicationFixture.fixture
import models.DatabeSuffix
import org.specs2.matcher._
import play.api.libs.json.Json
import play.api.mvc.Result

import scala.concurrent.Future

class BootApiV1Spec extends ApiSpecification {
  fixture.populate(DatabeSuffix("BOOT_"))
  "/api boot" should {
    s"return accepted a boot command" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot", Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
    s"return accepted a boot command with table suffix" in new Scope {
      val result: Future[Result] = routePOST(
        "/boot/BOOT_", Json.obj())
      status(result) must equalTo(ACCEPTED)
    }
  }

}