package models

import java.sql.Timestamp

case class Dump(author: String,
                rev: Long,
                message: Option[String],
                time: Option[Timestamp],
                path: String,
                typeModification: Option[Int],
                taskId: Long,
                taskType: Option[String],
                userStory: Option[Long],
                timeSpend: Option[Double])
