import org.specs2.matcher._
import api.Api._
import api.ApiError._
import play.api.mvc.Result

import scala.concurrent.Future
import play.api.libs.json.{JsNull, JsValue, Json}

class AuthControllerApiV1Spec extends ApiSpecification {

  private def signIn: Option[String] = {
    val result = routePOST("/api/v1/signin", Json.obj("email" -> "user1@mail.com", "password" -> "123456"))
    status(result) must equalTo(OK)
    val response = Json.parse(contentAsString(result))
    (response \ "token").asOpt[String]
  }

  "/api" should {
    s"warn if $HEADER_API_KEY header is not present" in {
      mustBeError(ERROR_APIKEY_NOTFOUND, routeGET("/api/v1/test", basicHeaders.remove(HEADER_API_KEY)))
    }
    s"warn if API KEY is unknown" in new Scope {
      mustBeError(ERROR_APIKEY_UNKNOWN, routeGET("/api/v1/test", basicHeaders.replace(HEADER_API_KEY -> "unknown_apikey")))
    }
    s"warn if $HEADER_AUTH_TOKEN is not present for a secured request" in new Scope {
      mustBeError(ERROR_TOKEN_HEADER_NOTFOUND, routeGET("/api/v1/account"))
    }
//    "send 404 on a bad request" in new Scope {
//      mustBeError(ERROR_NOTFOUND, routeGET("/boum"))
//    }
    "render correctly the test page" in new Scope {
      val result: Future[Result] = routeGET("/api/v1/test")
      status(result) must equalTo(OK)
      contentType(result) must beSome.which(_ == "application/json")
      contentAsString(result) must contain("The API is ready")
      header(HEADER_CONTENT_LANGUAGE, result) must beSome("en")
    }
    "respond in the requested language" in new Scope {
      val result: Future[Result] = routeGET("/api/v1/test", basicHeaders.replace(HEADER_ACCEPT_LANGUAGE -> "es"))
      status(result) must equalTo(OK)
      header(HEADER_CONTENT_LANGUAGE, result) must beSome("es")
    }
    "not respond to unauthorized requests" in new Scope {
      mustBeError(ERROR_TOKEN_HEADER_NOTFOUND, routeGET("/api/v1/account"))
    }
    "sign in" in new Scope {
      val result: Future[Result] = routePOST(
        "/api/v1/signin",
        Json.obj("email" -> "user1@mail.com", "password" -> "123456"))
      status(result) must equalTo(OK)
      val response: JsValue = Json.parse(contentAsString(result))
      (response \ "token").asOpt[String] must beSome
      (response \ "minutes").asOpt[Int] must beSome
    }
    "respond to authorized requests" in new Scope {
      signIn.map { token =>
        status(routeSecuredGET(token)("/api/v1/account")) must equalTo(OK)
      }
    }
    "sign out" in new Scope {
      signIn.map { token =>
        status(routeSecuredPOST(token)("/api/v1/signout", JsNull)) must equalTo(NO_CONTENT)
      }
    }
    "sign up is blocked" in new Scope {
      val result: Future[Result] = routePOST("/api/v1/signup",
        Json.obj("email" -> "user1@mail.com", "password" -> "123456", "user" -> "dummy_user"))
      status(result) must equalTo(ERROR_BADREQUEST)
    }
    "not respond to unauthorized requests once signed out" in new Scope {
      signIn.map { token =>
        val result = routeSecuredPOST(token)("/api/v1/signout", JsNull)
        status(result) must equalTo(NO_CONTENT)
        mustBeError(ERROR_TOKEN_NOTFOUND, routeSecuredGET(token)("/api/v1/account"))
      }
    }
  }
}
