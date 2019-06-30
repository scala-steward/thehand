import ApplicationFixture.fixture
import models.DatabaseSuffix
import org.specs2.matcher.Scope
import play.api.mvc.Result

import scala.concurrent.Future

class CommitFilesApiV1Spec extends ApiSpecification {
  fixture.populate(DatabaseSuffix("COMMIT_FILES_"))

  "/api commits files" should {
    s"return a list of commits files" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMIT_FILES_/commitfiles")
      status(result) must equalTo(OK)
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[{"typeModification":65,"copyRevision":1,"pathId":1,"revisionId":1,"id":1},
            {"typeModification":65,"copyRevision":1,"pathId":2,"revisionId":1,"id":2},
            {"typeModification":65,"copyRevision":1,"pathId":3,"revisionId":1,"id":3},
            {"typeModification":77,"copyRevision":4,"pathId":1,"revisionId":2,"id":4},
            {"typeModification":77,"copyRevision":2,"pathId":2,"revisionId":2,"id":5},
            {"typeModification":68,"copyRevision":5,"pathId":1,"revisionId":3,"id":6}]""").ignoreSpace
    }
    s"return a commit files by id" in new Scope {
      val result: Future[Result] = routeGET(
        "/api/v1/COMMIT_FILES_/commitfiles/3")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      val s = contentAsString(result)
      s must beEqualTo(
        s"""[
             {
               "typeModification": 65,
               "copyRevision": 1,
               "pathId": 3,
               "revisionId": 1,
               "id": 3
             }
           ]""").ignoreSpace
    }
  }
}
