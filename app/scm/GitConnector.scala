package scm

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.{ Ref, Repository }
import org.eclipse.jgit.revwalk.RevCommit
import telemetrics.HandLogger

import scala.collection.JavaConverters._

class GitConnector(repository: Repository) extends ScmConnector[RevCommit] {
  override def latestId: Long = {
    val git = new Git(repository)
    Seq(git.log.all).size.toLong
  }

  override def log(startRev: Long, endRev: Long): Seq[RevCommit] = {
    val git = new Git(repository)
    val logs = git.log().call().asScala.toSeq
    HandLogger.info("Had " + logs.size + " commits overall on current branch")
    logs
  }

  def logRemote(remoteUrl: String): Seq[Ref] = {
    Git.lsRemoteRepository()
      .setHeads(true)
      .setTags(true)
      .setRemote(remoteUrl)
      .call
      .asScala
      .toSeq
  }

  def logRemoteAsMap(remoteUrl: String): Seq[(String, Ref)] = {
    Git.lsRemoteRepository()
      .setHeads(true)
      .setTags(true)
      .setRemote(remoteUrl)
      .callAsMap
      .asScala
      .toSeq
  }

  def logRemoteAll(remoteUrl: String) = {
    Git.lsRemoteRepository()
      .call
      .asScala
      .toSeq
  }

  def logWithFilterBrach(includeBranch: String, filterBranch: String): Seq[RevCommit] = {
    val git = new Git(repository)
    git.log()
      .not(repository.resolve(filterBranch))
      .add(repository.resolve(includeBranch))
      .call
      .asScala
      .toSeq
  }

  def logWithFilterPath(path: String): Seq[RevCommit] = {
    val git = new Git(repository)
    git.log()
      .addPath(path)
      .call
      .asScala
      .toSeq
  }
}

