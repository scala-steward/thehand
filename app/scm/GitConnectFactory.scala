package scm

import java.io.File

import org.eclipse.jgit.api.Git
import org.tmatesoft.svn.core.SVNURL
import org.tmatesoft.svn.core.io.{SVNRepository, SVNRepositoryFactory}
import org.tmatesoft.svn.core.wc.SVNWCUtil
import telemetrics.HandLogger

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait GitConnectFactory {
  def auth(repository: SVNRepository, name: String, password: String): Git = ???

  def init(url: String): Try[Any] = ???

  def sshUrlBuild(user: String, pwd: String, host: String, pathToRemote: String, port: Long = 22) = {
    s"ssh://<${user}>:<${pwd}>@<${host}>:${port}/${pathToRemote}/"
  }

  def clone(remoteUrl: String, localPath: String) = {
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
