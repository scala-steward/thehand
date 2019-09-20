package conf

import com.typesafe.config.ConfigFactory
import play.api.Configuration

object BootConf  {
  private lazy val conf = new Configuration(ConfigFactory.load())
  lazy val magic: String = conf.get[String]("boot.magic")
  lazy val first_api_key: String = conf.get[String]("boot.first_api_key")
}