package reportio

trait ReportIO {
  def write(filename: String, lines: Seq[(String, Int)]): Unit
  def writeSLI(filename: String, lines: Seq[(String, Long, Int)]): Unit
}
