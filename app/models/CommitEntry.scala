/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json._

case class CommitEntry(
  message: Option[String],
  timestamp: Option[Timestamp],
  revision: Long,
  authorId: Long,
  id: Long = 0L)

object CommitEntry {
  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val commitEntryFormat: OFormat[CommitEntry] = Json.format[CommitEntry]
}
