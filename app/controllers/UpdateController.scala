/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers

import javax.inject._
import dao._
import models.Suffix
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class UpdateController @Inject()(dao: UpdateDao,
                                 cc: MessagesControllerComponents
                                )
  extends MessagesAbstractController(cc) {

  //implicit val suffix: Suffix = Suffix("eb_")

  def updateAll() = Action {
    dao.updateAll()
    Ok("Updated")
  }
}