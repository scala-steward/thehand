package models

import java.sql.Timestamp

import reportio.Writable

class DumpJoinDatabase(authorId: Long,
                       author: String,
                       rev: Long,
                       message: Option[String],
                       time: Option[Timestamp],
                       fileId: Long,
                       path: String,
                       typeModification: Option[Int],
                       taskId: Long,
                       taskType: Option[String],
                       userStory: Option[Long],
                       timeSpend: Option[Double]) extends Writable
{
  override def toWritableSeq: Seq[Any] = {
    List(this.authorId,
      this.author,
      this.rev,
      this.message.getOrElse(""),
      this.fileId,
      this.path,
      this.time.getOrElse(""),
      this.typeModification.getOrElse(""),
      this.taskId,
      this.taskType.getOrElse(""),
      this.userStory.getOrElse(""),
      this.timeSpend.getOrElse(0.0)
    )
  }
}

object DumpJoinDatabase {
  def apply(authorId: Long,
            author: String,
            rev: Long,
            message: Option[String],
            time: Option[Timestamp],
            fileId: Long,
            path: String,
            typeModification: Option[Int],
            taskId: Long,
            taskType: Option[String],
            userStory: Option[Long],
            timeSpend: Option[Double]): DumpJoinDatabase = new DumpJoinDatabase(authorId, author, rev, message, time, fileId, path, typeModification, taskId, taskType, userStory, timeSpend)
}