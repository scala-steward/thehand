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
        s"""[{"path":"/zip","id":1},{"path":"/zap","id":2},{"path":"/zop","id":3}]""").ignoreSpace
    }
    s"return a files by id" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/FILES_/files/2")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[
             {
               "path": "/zap",
               "id": 2
             }
           ]""").ignoreSpace
    }
    // HIRO check this below !!!
    s"return a file path and a counter for all tasks of type bug" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/FILES_/files/bugs")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[
             [
               "/zap",
               1
             ],
             [
               "/zip",
               2
             ]
           ]""").ignoreSpace
    }
  }
}
