package scm

trait ScmConnector[T] {
  def log(startRev: Long, endRev: Long): Seq[T]
  def latestId: Option[Long]
}
