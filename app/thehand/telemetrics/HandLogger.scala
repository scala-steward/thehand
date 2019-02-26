/*
 * Copyright (c) 2018, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 *
 *
 */

package thehand.telemetrics

import com.typesafe.scalalogging.LazyLogging

object HandLogger extends LazyLogging {
  def debug(m: String): Unit = logger.debug(m)
  def info(m: String): Unit = logger.info(m)
  def error(m: String): Unit = logger.error(m)
  def warn(m: String): Unit = logger.warn(m)
}