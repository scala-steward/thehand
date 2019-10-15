import ApplicationFixture.fixture
import models.DatabaseSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class FilesApiV1Spec extends ApiSpecification {
  fixture.populate(DatabaseSuffix("FILES_"))
  "/api author" should {
    s"return a list of files" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/FILES_/files")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"path":"${ExtractorFixture.file2}","id":1},{"path":"${ExtractorFixture.file3}","id":2},{"path":"${ExtractorFixture.file1}","id":3}]""").ignoreSpace
    }
    s"return a files by id" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/FILES_/files/2")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"path":"${ExtractorFixture.file3}","id":2}]""").ignoreSpace
    }
    // HIRO check this below !!!
    s"return a file path and a counter for all tasks of type bug" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/FILES_/files/bugs")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[["${ExtractorFixture.file2}",1],["${ExtractorFixture.file1}",2]]""").ignoreSpace
    }
  }
}
