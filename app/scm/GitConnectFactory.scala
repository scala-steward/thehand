package scm

import java.io.File

import org.eclipse.jgit.api.Git
import org.tmatesoft.svn.core.io.SVNRepository
import scala.util.Try

trait GitConnectFactory {
  def auth(repository: SVNRepository, name: String, password: String): Git = ???

  def init(url: String): Try[Any] = ???

  def sshUrlBuild(user: String, pwd: String, host: String, pathToRemote: String, port: Long = 22): String = {
    s"ssh://<$user>:<$pwd>@<$host>:$port/$pathToRemote/"
  }

  def clone(remoteUrl: String, localPath: String): Git = {
    val builder: GitCredentialProvider = new GitCredentialProvider()
    val allowHosts = builder.Build()
    val localPathFile = new File(localPath)
    val result = Git.cloneRepository()
      .setURI(remoteUrl)
      .setDirectory(localPathFile)
      .setCredentialsProvider(allowHosts)
      .call()
    result
  }

}
