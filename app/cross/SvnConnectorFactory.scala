package cross

import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.io.{SVNRepository, SVNRepositoryFactory}
import org.tmatesoft.svn.core.wc.SVNWCUtil
import thehand.telemetrics.HandLogger

import scala.util.{Failure, Success, Try}

trait SvnConnectorFactory {
  private def auth(repository: SVNRepository, name: String, password: String): SVNRepository = {
    val authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password.toCharArray)
    repository.setAuthenticationManager(authManager)
    repository
  }

  private def init(url: String): Try[SVNRepository] = Try {
    SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url))
  }

  def connect(url: String, name: String, password: String): Option[SvnConnector] = init(url) match {
    case Success(r) => Some(new SvnConnector(auth(r, name, password)))
    case Failure(e) =>
      HandLogger.error("error while creating an SVNRepository for the location '" + url + "': " + e.getMessage)
      None
  }
}
