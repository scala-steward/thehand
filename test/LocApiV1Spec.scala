import ApplicationFixture.fixture
import api.Api.{HEADER_ACCEPT_LANGUAGE, HEADER_API_KEY, HEADER_CONTENT_TYPE}
import models.{DatabaseSuffix, LocFile}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.Scope
import play.api.libs.json.Json
import play.api.mvc.{Headers, Result}

import scala.concurrent.Future
import scala.concurrent.duration._

class LocApiV1Spec(implicit ee: ExecutionEnv) extends ApiSpecification {
  val localSuffix = DatabaseSuffix("LOC_")
  fixture.populate(localSuffix)

  val counterXml = s"""<?xml version="1.0" encoding="UTF-8"?>
                     |  <sourcemonitor_metrics>
                     |    <project version="3.4">
                     |      <checkpoints checkpoint_count="1">
                     |       <checkpoint>
                     |         <files file_count="2">
                     |           <file file_name="${ExtractorFixture.file2}">
                     |             <metrics metric_count="15">
                     |               <metric id="M0">459</metric>
                     |               <metric id="M1">45</metric>
                     |             </metrics>
                     |           </file>
                     |           <file file_name="${ExtractorFixture.file1}">
                     |             <metrics metric_count="15">
                     |               <metric id="M0">459</metric>
                     |               <metric id="M1">45</metric>
                     |             </metrics>
                     |           </file>
                     |         </files>
                     |       </checkpoint>
                     |     </checkpoints>
                     |   </project>
                     |  </sourcemonitor_metrics>""".stripMargin

  val xmlHeaders = Headers(
    HEADER_CONTENT_TYPE -> "application/xml",
    HEADER_ACCEPT_LANGUAGE -> "en",
    HEADER_API_KEY -> "AbCdEfGhIjK1")


  val counterJson = Json.arr(
    Json.obj("path" -> ExtractorFixture.file2, "counter" ->  359),
    Json.obj("path" -> ExtractorFixture.file1, "counter" ->  359))

  "/loc" should {
    "allow upload xml loc file" in new Scope {
      val result: Future[Result] = routePOST(
        s"/api/v1/${localSuffix.suffix}/locxml",
        counterXml, xmlHeaders)
      status(result) must equalTo(OK)

      lazy val vec = Vector(LocFile(3,459,1), LocFile(1,459,2), LocFile(2,20,3))
      lazy val lineCounter = fixture.daoLineCounter.list(localSuffix)
      lineCounter must equalTo[Seq[LocFile]](vec).await(retries = 2, timeout = 1.seconds)
    }
    "allow upload json loc file" in new Scope {
      val result: Future[Result] = routePOST(
        s"/api/v1/${localSuffix.suffix}/loc", counterJson)
      status(result) must equalTo(OK)

      lazy val vec = Vector(LocFile(3,359,1), LocFile(1,359,2), LocFile(2,20,3))
      lazy val lineCounter = fixture.daoLineCounter.list(localSuffix)
      lineCounter must equalTo[Seq[LocFile]](vec).await(retries = 2, timeout = 1.seconds)
    }
  }

}
