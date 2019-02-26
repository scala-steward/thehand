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
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class TaskController @Inject()(repo: TaskRepository,
                                 cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
    * The mapping for the person form.
    */
//  val personForm: Form[CreatePersonForm] = Form {
//    mapping(
//      "name" -> nonEmptyText,
//      "age" -> number.verifying(min(0), max(140))
//    )(CreatePersonForm.apply)(CreatePersonForm.unapply)
//  }

  /**
    * The index action.
    */
//  def index = Action { implicit request =>
//    Ok(views.html.index(personForm))
//  }

  /**
    * The add person action.
    *
    * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
    */
  //  def addPerson = Action.async { implicit request =>
  //    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
  //    personForm.bindFromRequest.fold(
  //      // The error function. We return the index page with the error form, which will render the errors.
  //      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
  //      // a future because the person creation function returns a future.
  //      errorForm => {
  //        Future.successful(Ok(views.html.index(errorForm)))
  //      },
  //      // There were no errors in the from, so create the person.
  //      person => {
  //        repo.create(person.name, person.age).map { _ =>
  //          // If successful, we simply redirect to the index page.
  //          Redirect(routes.PersonController.index).flashing("success" -> "user.created")
  //        }
  //      }
  //    )
  //  }

  /**
    * A REST endpoint that gets all the people as JSON.
    */
  def getTasks = Action.async { implicit request =>
    repo.list_s("eb_").map { task =>
      Ok(Json.toJson(task))
    }
  }
}

/**
  * The create person form.
  *
  * Generally for forms, you should define separate objects to your models, since forms very often need to present data
  * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
  * that is generated once it's created.
  */
case class CreateTaskForm(name: String, age: Int)
