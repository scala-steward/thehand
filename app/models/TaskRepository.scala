/*
 * Copyright (c) 2019, Jeison Cardoso. All Rights Reserved
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE as published by
 * the Free Software Foundation; either version 3, or (at your option)
 * any later version.
 */

package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

/**
  * A repository for taks.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class TaskRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

//  object TaskTable {
//    def apply(tag: Tag, suffix: String): TaskTable = new TaskTable(tag, suffix)
//  }

  private class TaskTable(tag: Tag, suffix: String) extends Table[Task](tag, suffix + "task") {
    def typeTask: Rep[Option[String]] = column[Option[String]]("type_task")
    def typeTaskId: Rep[Option[Long]] = column[Option[Long]]("type_task_id")
    def timeSpend: Rep[Option[Double]] = column[Option[Double]]("time_spend")
    def parentId: Rep[Option[Long]] = column[Option[Long]]("parent_id")
    def taskId: Rep[Long] = column[Long]("task_id", O.Unique)
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    //def * : ProvenShape[Task] = (typeTask, typeTaskId, timeSpend, parentId, taskId, id).mapTo[Task]
    def * : ProvenShape[Task] = (typeTask, typeTaskId, timeSpend, parentId, taskId, id) <> ((Task.apply _).tupled, Task.unapply)
  }

//  private class TaskTable(tag: Tag) extends Table[Task](tag, "people") {
//
//    /** The ID column, which is the primary key, and auto incremented */
//    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
//
//    /** The name column */
//    def name = column[String]("name")
//
//    /** The age column */
//    def age = column[Int]("age")
//
//    /**
//      * This is the tables default "projection".
//      *
//      * It defines how the columns are converted to and from the Person object.
//      *
//      * In this case, we are simply passing the id, name and page parameters to the Person case classes
//      * apply and unapply methods.
//      */
//    def * = (id, name, age) <> ((Person.apply _).tupled, Person.unapply)
//  }

  /**
    * The starting point for all queries on the people table.
    */
  private val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, "eb_"))

  //  def create(name: String, age: Int): Future[Person] = db.run {
  //    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
  //    (people.map(p => (p.name, p.age))
  //      // Now define it to return the id, because we want to know what id was generated for the person
  //      returning people.map(_.id)
  //      // And we define a transformation for the returned value, which combines our original parameters with the
  //      // returned id
  //      into ((nameAge, id) => Person(id, nameAge._1, nameAge._2))
  //    // And finally, insert the person into the database
  //    ) += (name, age)
  //  }

  def list_s(suffix : String): Future[Seq[Task]] = db.run {
    lazy val tasks = TableQuery[TaskTable]((tag: Tag) => new TaskTable(tag, suffix))
    tasks.result
  }

  def list(): Future[Seq[Task]] = db.run {
    tasks.result
  }
}
