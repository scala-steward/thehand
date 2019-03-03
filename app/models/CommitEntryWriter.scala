/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import play.api.libs.json.{Json, OFormat}

case class CommitEntryWriter(commit: CommitEntryFile,
                              path: String,
                              pathCopy: String)


object CommitEntryWriter {
  implicit val commitEntryFormat: OFormat[CommitEntryWriter] = Json.format[CommitEntryWriter]
}