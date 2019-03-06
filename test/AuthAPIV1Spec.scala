import org.specs2.matcher._
import play.api.test._
import play.api.inject.guice.GuiceApplicationBuilder
import api._
import api.Api._
import api.ApiError._
import dao.BootstrapDAO
import org.joda.time.DateTime
import play.api.Application
import play.api.mvc.{ AnyContentAsEmpty, Headers }
import play.api.http.Writeable
import play.api.mvc.Result

import scala.concurrent.Future
import scala.util.Try
import play.api.libs.json.{ JsNull, JsValue, Json }
import telemetrics.HandLogger

class AuthAPIV1Spec extends PlaySpecification with JsonMatchers {

  lazy val app = new GuiceApplicationBuilder().
    configure(
      "slick.dbs.mydb.driver" -> "slick.driver.H2Driver$",
      "slick.dbs.mydb.db.driver" -> "org.h2.Driver",
      "slick.dbs.mydb.db.url" -> "jdbc:h2:mem:blah;",
      "slick.dbs.mydb.db.user" -> "test",
      "slick.dbs.mydb.db.password" -> "").build

  val daoBootstrap = Application.instanceCache[BootstrapDAO].apply(app)
  daoBootstrap.createSchemas()

  val fixture = Application.instanceCache[DatabaseFixture].apply(app)
  fixture.populate()

  val basicHeaders = Headers(
    HEADER_CONTENT_TYPE -> "application/json",
    HEADER_ACCEPT_LANGUAGE -> "en",
    HEADER_DATE -> printHeaderDate(new DateTime()),
    HEADER_API_KEY -> "AbCdEfGhIjK1")
  def basicHeadersWithToken(token: String): Headers = basicHeaders.add(HEADER_AUTH_TOKEN -> token)

  def routeGET(uri: String, headers: Headers = basicHeaders): Future[Result] = getRoute(GET, uri, AnyContentAsEmpty, headers)
  def routePOST[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] = getRoute(POST, uri, body, headers)
  def routePUT[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] = getRoute(PUT, uri, body, headers)
  def routePATCH[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] = getRoute(PATCH, uri, body, headers)
  def routeDELETE(uri: String, headers: Headers = basicHeaders): Future[Result] = getRoute(DELETE, uri, AnyContentAsEmpty, headers)
  def routeSecuredGET(token: String)(uri: String, headers: Headers = basicHeadersWithToken(token)): Future[Result] = routeGET(uri, headers)
  def routeSecuredPOST[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] = routePOST(uri, body, headers)
  def routeSecuredPUT[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] = routePUT(uri, body, headers)
  def routeSecuredPATCH[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] = routePATCH(uri, body, headers)
  def routeSecuredDELETE(token: String)(uri: String, headers: Headers = basicHeadersWithToken(token)): Future[Result] = routeDELETE(uri, headers)
  def getRoute[A](method: String, uri: String, body: A, headers: Headers)(implicit w: Writeable[A]): Future[Result] = route(app, FakeRequest(method, uri, headers, body)).get

  def mustBeError(code: Int, result: Future[Result]): MatchResult[String] = {
    status(result) must equalTo(ApiError.statusFromCode(code))
    contentAsString(result) must /("code" -> code)
  }

  def signIn: Option[String] = {
    val result = routePOST("/api/v1/signin", Json.obj("email" -> "user1@mail.com", "password" -> "123456"))
    status(result) must equalTo(OK)
    val response = Json.parse(contentAsString(result))
    (response \ "token").asOpt[String]
  }

  "/api" should {

    s"warn if $HEADER_API_KEY header is not present" in {
      mustBeError(ERROR_APIKEY_NOTFOUND, routeGET("/api/v1/test", basicHeaders.remove(HEADER_API_KEY)))
    }
    s"warn if $HEADER_DATE header is not present" in new Scope {
      mustBeError(ERROR_DATE_NOTFOUND, routeGET("/api/v1/test", basicHeaders.remove(HEADER_DATE)))
    }
    s"warn if $HEADER_DATE header is malformed" in new Scope {
      mustBeError(ERROR_DATE_MALFORMED, routeGET("/api/v1/test", basicHeaders.replace(HEADER_DATE -> "malformed_date")))
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
      val maybeDate: Option[String] = header(HEADER_DATE, result)
      maybeDate must beSome
      maybeDate.map { dateString =>
        Try(Api.parseHeaderDate(dateString)) must beSuccessfulTry
      }
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
    "paginate correctly" in new Scope {
      signIn.map { token =>
        val result = routeSecuredGET(token)("/api/v1/phases")
        status(result) must equalTo(OK)
        header(HEADER_PAGE, result) must beSome
        header(HEADER_PAGE_FROM, result) must beSome
        header(HEADER_PAGE_SIZE, result) must beSome
        header(HEADER_PAGE_TOTAL, result) must beSome
      }
    }
    "not respond to unauthorized requests once signed out" in new Scope {
      signIn.map { token =>
        routeSecuredPOST(token)("/api/v1/signout", JsNull)
        mustBeError(ERROR_TOKEN_NOTFOUND, routeSecuredGET(token)("/api/v1/account"))
      }
    }
  }
}
