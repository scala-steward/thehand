package api

import play.api.i18n.Lang
import play.api.mvc.{ Call, RequestHeader }

object Api {
  // Headers
  final val HEADER_CONTENT_TYPE = "Content-Type"
  final val HEADER_CONTENT_LANGUAGE = "Content-Language"
  final val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
  final val HEADER_LOCATION = "Location"
  final val HEADER_API_KEY = "X-API-Key"
  final val HEADER_AUTH_TOKEN = "X-AUTH-Token"

  final val HEADER_PAGE = "X-Page"
  final val HEADER_PAGE_FROM = "X-Page-From"
  final val HEADER_PAGE_SIZE = "X-Page-Size"
  final val HEADER_PAGE_TOTAL = "X-Page-Total"

  def basicHeaders(implicit lang: Lang): Seq[(String, String)] = Seq(
    HEADER_CONTENT_LANGUAGE -> lang.language)

  def locationHeader(uri: String): (String, String) = HEADER_LOCATION -> uri
  def locationHeader(call: Call)(implicit request: RequestHeader): (String, String) = locationHeader(call.absoluteURL())

  object Sorting {
    final val ASC = false
    final val DESC = true
  }
}