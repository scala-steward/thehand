package reportio

trait Writable {
  def toWritableSeq: Seq[Any]
}

trait ReportIO {
  def write(filename: String, w: Seq[Writable]): Unit
}
