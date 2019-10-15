package reportio

object CvsIO extends ReportIO {
  def write(list: Seq[Writable]): String = {
    list.map(_.toWritableSeq.map(_.toString).mkString(",")).mkString("\n")
  }
}
