import api.Api.{HEADER_ACCEPT_LANGUAGE, HEADER_API_KEY, HEADER_AUTH_TOKEN, HEADER_CONTENT_TYPE}
import api.ApiError
import org.specs2.matcher.{JsonMatchers, MatchResult}
import play.api.http.Writeable
import play.api.mvc.{AnyContentAsEmpty, Headers, Result}
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.Future

class ApiSpecification extends PlaySpecification with JsonMatchers {
  ApplicationFixture.initializeWithData()

  val basicHeaders = Headers(
    HEADER_CONTENT_TYPE -> "application/json",
    HEADER_ACCEPT_LANGUAGE -> "en",
    HEADER_API_KEY -> "AbCdEfGhIjK1")

  def basicHeadersWithToken(token: String): Headers = basicHeaders.add(HEADER_AUTH_TOKEN -> token)

  def routeGET(uri: String, headers: Headers = basicHeaders): Future[Result] =
    getRoute(GET, uri, AnyContentAsEmpty, headers)

  def routePOST[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] =
    getRoute(POST, uri, body, headers)

  def routePUT[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] =
    getRoute(PUT, uri, body, headers)

  def routePATCH[A](uri: String, body: A, headers: Headers = basicHeaders)(implicit w: Writeable[A]): Future[Result] =
    getRoute(PATCH, uri, body, headers)

  def routeDELETE(uri: String, headers: Headers = basicHeaders): Future[Result] =
    getRoute(DELETE, uri, AnyContentAsEmpty, headers)

  def routeSecuredGET(token: String)(uri: String, headers: Headers = basicHeadersWithToken(token)): Future[Result] =
    routeGET(uri, headers)

  def routeSecuredPOST[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] =
    routePOST(uri, body, headers)

  def routeSecuredPUT[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] =
    routePUT(uri, body, headers)

  def routeSecuredPATCH[A](token: String)(uri: String, body: A, headers: Headers = basicHeadersWithToken(token))(implicit w: Writeable[A]): Future[Result] =
    routePATCH(uri, body, headers)

  def routeSecuredDELETE(token: String)(uri: String, headers: Headers = basicHeadersWithToken(token)): Future[Result] =
    routeDELETE(uri, headers)

  def getRoute[A](method: String, uri: String, body: A, headers: Headers)(implicit w: Writeable[A]): Future[Result] =
    route(ApplicationFixture.app, FakeRequest(method, uri, headers, body)).get

  def mustBeError(code: Int, result: Future[Result]): MatchResult[String] = {
    status(result) must equalTo(ApiError.statusFromCode(code))
    contentAsString(result) must /("code" -> code)
  }

}
