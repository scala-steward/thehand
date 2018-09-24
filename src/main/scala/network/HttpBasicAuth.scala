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

package network

import org.apache.commons.codec.binary.Base64

object HttpBasicAuth {
  val BASIC = "Basic"
  val AUTHORIZATION = "Authorization"

  def encodeCredentials(username: String, password: String): String = {
    Base64.encodeBase64String((username + ":" + password).getBytes)
  }

  def getHeader(username: String, password: String): String =
    BASIC + " " + encodeCredentials(username, password)
}
