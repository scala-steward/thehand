package api

import Api._
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json._

trait ApiRequestHeader[R <: RequestHeader] {
  val request: R
  val apiKeyOpt: Option[String] = request.headers.get(HEADER_API_KEY)
  val tokenOpt: Option[String] = request.headers.get(HEADER_AUTH_TOKEN)
  def RremoteAddress: String = request.remoteAddress
  def method: String = request.method
  def Uuri: String = request.uri
  def maybeBody: Option[String] = None
}

case class ApiRequestHeaderImpl(request: RequestHeader) extends ApiRequestHeader[RequestHeader]

/*
* ApiRequestHeader for requests that don't require authentication
*/
class ApiRequest[A](val request: Request[A]) extends WrappedRequest[A](request) with ApiRequestHeader[Request[A]] {
  override def RremoteAddress: String = request.remoteAddress
  override def method: String = request.method
  override def Uuri: String = request.uri
  override def maybeBody: Option[String] = request.body match {
    case body: JsValue => Some(Json.prettyPrint(body))
    case body: String => if (body.length > 0) Some(body) else None
    case body => Some(body.toString)
  }
}
object ApiRequest {
  def apply[A](request: Request[A]): ApiRequest[A] = new ApiRequest[A](request)
}

/*
* ApiRequest for user aware requests
*/
case class UserAwareApiRequest[A](override val request: Request[A], apiKey: String, date: DateTime, token: Option[String], userId: Option[Long]) extends ApiRequest[A](request) {
  def isLogged: Boolean = userId.isDefined
}

/*
* ApiRequest for authenticated requests
*/
case class SecuredApiRequest[A](override val request: Request[A], apiKey: String, date: DateTime, token: String, userId: Long) extends ApiRequest[A](request)

