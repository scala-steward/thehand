/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import play.api.libs.json.{ Json, OFormat }

case class Project(
  name: String,
  defaultMode: Option[Int],
  scmId: Long,
  taskManagerId: Long,
  id: Long = 0L)

object Project {
  implicit val personFormat: OFormat[Project] = Json.format[Project]
}