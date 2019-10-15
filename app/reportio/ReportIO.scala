package reportio

trait Writable {
  def toWritableSeq: Seq[Any]
}

trait ReportIO {
  def write(w: Seq[Writable]): String
}
