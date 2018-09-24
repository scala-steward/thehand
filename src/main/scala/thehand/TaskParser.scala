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

package thehand

trait TaskParser {
  def parse(s: String): Seq[String]
  def parseConcat(s: String): String
  def convert(s: String): Array[Long]
  def convertMessage(s:  Option[String]): Seq[Long]
}

case class TaskParserCharp(patternParser: String, patterSplit: String, separator: String) extends TaskParser {
  def parse(s: String): Seq[String] = {
    (patternParser.r findAllIn s).toSeq
  }

  def convert(s: String): Array[Long] = {
    s.split(patterSplit)
      .filter(!_.isEmpty)
      .map(_.toLong)
  }

  def parseConcat(s: String) : String = {
    var v = ""
    parse(s).foreach( v += _)
    v
  }

  def convertMessage(s: Option[String]): Seq[Long] = s match {
    case Some(v) => parse(v).flatMap(convert)
    case None => Nil
  }
}
