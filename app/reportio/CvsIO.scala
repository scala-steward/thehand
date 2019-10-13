package reportio

import java.io.File

import com.github.tototoshi.csv.CSVWriter

object CvsIO extends ReportIO {
  def write(filename: String, lines: Seq[(String, Int)]): Unit = {
    lazy val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    lines.sortBy{ case(_, counter) => counter }
      .reverse
      .map{ case(path, counter) => List(counter, path) }
      .foreach(row => writer.writeRow(row))
    writer.close()
  }
  def writeSLI(filename: String, lines: Seq[(String, Long, Int)]): Unit = {
    lazy val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    lines
      .sortBy{ case(_, _, counter) => counter }
      .reverse
      .map{ case(path, lineCounter, counter) => List(counter, lineCounter, path) }
      .foreach(row => writer.writeRow(row))
    writer.close()
  }
}

