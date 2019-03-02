package reportio

trait ReportIO {
  def write(filename: String, lines: Seq[(String, Int)]) : Unit
}
