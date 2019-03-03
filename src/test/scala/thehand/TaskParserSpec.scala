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

import org.scalatest.{ FlatSpec, Matchers }

class TaskParserSpec extends FlatSpec with Matchers {
  //#NUMBER
  val patternParser = "(#\\d)\\d+"
  val patternSplit = "#"
  val separator = ""

  val defaultParser = TaskParserCharp(patternParser, patternSplit, separator)

  "Convert message #123 TEST" should "return id 123" in {
    defaultParser.convert(Some("#123 TEST")) shouldEqual (Seq(123))
  }

  "Convert message #123 #145 TEST" should "return ids 123 145" in {
    defaultParser.convert(Some("#123 #145 TEST")) shouldEqual (Seq(123, 145))
  }

  "Convert message #123#145#67 TEST" should "return ids 123, 145, 67" in {
    defaultParser.convert(Some("#123#145#67 TEST")) shouldEqual (Seq(123, 145, 67))
  }

  "Convert message #TEST" should "no return id" in {
    defaultParser.convert(Some("#TEST")) shouldEqual (Nil)
  }

  "Convert no message" should "no return id" in {
    defaultParser.convert(None) shouldEqual (Nil)
  }

}
