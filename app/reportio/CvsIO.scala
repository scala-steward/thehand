package reportio

import java.sql.Timestamp

object CvsIO extends ReportIO {
  def write(list: Seq[Writable]): String = {
    list.map(_.toWritableSeq.map(container).mkString(",")).mkString("\n")
  }

  def container(s: Any): String = s match {
    case s: String => "\""+s+"\""
    case s: Timestamp => "\""+s.toString+"\""
    case s => s.toString
  }
}
