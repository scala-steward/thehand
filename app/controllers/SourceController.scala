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

import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class SourceController @Inject()(repo: SourceRepository,
                                 cc: MessagesControllerComponents
                              )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getAuthors = Action.async { implicit request =>
    repo.listAuthors_s("eb_").map { t =>
      Ok(Json.toJson(t))
    }
  }

  def getCommits = Action.async { implicit request =>
    repo.listCommit_s("eb_").map { t =>
      Ok(Json.toJson(t))
    }
  }

//  def getCommitEntryFiles = Action.async { implicit request =>
//    repo.listCommitEntryFile_s("eb_").map { t =>
//      Ok(Json.toJson(t))
//    }
//  }

  def getCommitsTasks = Action.async { implicit request =>
    repo.listCommitTasks_s("eb_").map { t =>
      Ok(Json.toJson(t))
    }
  }

  def getEntryFiles = Action.async { implicit request =>
    repo.listEntryFiles_s("eb_").map { t =>
      Ok(Json.toJson(t))
    }
  }
}
