import org.specs2.matcher.Scope
import play.api.libs.json.Json
import play.api.mvc.Result

import scala.concurrent.Future

class PhasesApiV1Spec extends ApiSpecification {
  private def signIn: Option[String] = {
    val result = routePOST("/api/v1/signin", Json.obj("email" -> "user1@mail.com", "password" -> "123456"))
    status(result) must equalTo(OK)
    val response = Json.parse(contentAsString(result))
    (response \ "token").asOpt[String]
  }

  "/api phases after login" should {
//    s"return a list of phases" in new Scope {
//      signIn.map { token =>
//        val result: Future[Result] = routeSecuredGET(token)(
//          "/api/v1/phases")
//        status(result) must equalTo(OK)
//        val s = contentAsString(result)
//        s must beEqualTo(
//          s"""[{"userId":1,"order":0,"name":"Personal","id":1},
//             {"userId":1,"order":0,"name":"Personal","id":4},
//             {"userId":1,"order":0,"name":"Personal","id":7},
//             {"userId":1,"order":0,"name":"Personal","id":10},
//             {"userId":1,"order":0,"name":"Personal","id":13},
//             {"userId":1,"order":0,"name":"Personal","id":16},
//             {"userId":1,"order":0,"name":"Personal","id":19},
//             {"userId":1,"order":1,"name":"Work","id":2},
//             {"userId":1,"order":1,"name":"Work","id":5},
//             {"userId":1,"order":1,"name":"Work","id":8}]""").ignoreSpace
//      }
//    }
    s"return a phase with id one" in new Scope {
      signIn.map { token =>
        val result: Future[Result] = routeSecuredGET(token)(
          "/api/v1/phases/1")
        status(result) must equalTo(OK)
        val s = contentAsString(result)
        s must beEqualTo(
          s"""{"userId":1,"order":0,"name":"Personal","id":1}""").ignoreSpace
      }
    }
  }
}