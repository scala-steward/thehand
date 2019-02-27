/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.{JodaWrites, JodaReads}


case class CommitEntry(message: Option[String],
                             date: Option[DateTime],
                             revision: Long,
                             authorId: Long,
                             id: Long = 0L)

object CommitEntry {
  implicit val dateTimeWriter: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd HH:mm:ss.SSS")
  implicit val dateTimeJsReader: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss.SSS")
  implicit val commitEntryFormat: OFormat[CommitEntry] = Json.format[CommitEntry]
}
