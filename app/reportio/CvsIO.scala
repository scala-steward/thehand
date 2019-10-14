package reportio

import java.io.File

import com.github.tototoshi.csv.CSVWriter

object CvsIO extends ReportIO {
  def write(filename: String, w: Seq[Writable]): Unit = {
    lazy val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    w.map(_.toWritableSeq).foreach(row => writer.writeRow(row))
  }
}

