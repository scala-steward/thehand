import network.HttpBasicAuth
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

class HttpBasicAuthControllerSpec extends Specification with Matchers {

  s2"Encode credential return in 64encode $e1"
  val e1 = HttpBasicAuth
    .encodeCredentials("username", "pass") must beEqualTo[String]("dXNlcm5hbWU6cGFzcw==")

  s2"Encode empty credential return empty (Og==) $e2"
  val e2 = HttpBasicAuth
    .encodeCredentials("", "") must beEqualTo[String]("Og==")

  s2"Create header with user and pass must return a Basic 64encode $e3"
  val e3 = HttpBasicAuth
    .getHeader("username", "pass") must beEqualTo[String]("Basic dXNlcm5hbWU6cGFzcw==")

}
