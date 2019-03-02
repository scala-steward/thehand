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

package tasks

trait TaskParser {
  def convert(s: Option[String]): Seq[Long]
}

case class TaskParserCharp(patternParser: String, patternSplit: String, separator: String) extends TaskParser {
  private def parse(s: String): Seq[String] = {
    (patternParser.r findAllIn s).toSeq
  }

  private def slipt(s: String): Array[Long] = {
    s.split(patternSplit)
      .filter(!_.isEmpty)
      .map(_.toLong)
  }

  def convert(s: Option[String]): Seq[Long] = s match {
    case Some(v) => parse(v).flatMap(slipt)
    case None => Nil
  }
}
