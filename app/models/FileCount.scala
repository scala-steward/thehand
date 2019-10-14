package models

class FileCount(val path: String, val lines: Long)
object FileCount {
  def apply(path: String, lines: Long): FileCount = new FileCount(path.replace("\\", "/"), lines)
}
