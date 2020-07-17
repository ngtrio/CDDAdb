package cddadb.parser

import org.scalatest.wordspec.AnyWordSpecLike

class POParserTest extends AnyWordSpecLike {
  "_" in {
    val file = "data/zh.po"
    POParser().fromFile(file).parse.foreach(println)
  }
}
