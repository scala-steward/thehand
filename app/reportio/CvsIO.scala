package reportio

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import models.Dump

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

  def writeDump(filename: String, lines: Seq[Dump]): Unit = {
    lazy val f = new File(filename + ".csv")
    val writer = CSVWriter.open(f)
    lines.foreach(row => writer.writeRow(
      List(row.authorId,
        row.author,
        row.rev,
        row.message.getOrElse(""),
        row.fileId,
        row.path,
        row.time.getOrElse(""),
        row.typeModification.getOrElse(""),
        row.taskId,
        row.taskType.getOrElse(""),
        row.userStory.getOrElse(""),
        row.timeSpend.getOrElse(0.0)
      )))
    writer.close()
  }
}

