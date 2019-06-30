package conf

import com.typesafe.config.ConfigFactory
import play.api.Configuration

object TargetConf  {
  private lazy val conf = new Configuration(ConfigFactory.load())
  lazy val auth = BasicAuthConf(conf.get[String]("target.url"),
                      conf.get[String]("target.user"),
                      conf.get[String]("target.pass"))
}