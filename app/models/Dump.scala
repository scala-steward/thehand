package models

import java.sql.Timestamp

case class Dump(authorId: Long,
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
                timeSpend: Option[Double])
