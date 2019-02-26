/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package controllers



import cross.MainControl.update
import javax.inject._
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class HandController @Inject()(repo: SourceRepository,
                                 cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getUpdate = Action.async { implicit request =>
    lazy val repositories = Seq(
      "repository_eberick",
      "repository_qibulder",
      "repository_qi4d"
    )
    Future { update(repositories) }.map {
      case _ => Ok("Got update")
      //case e => InternalServerError(e)
    }
  }
}