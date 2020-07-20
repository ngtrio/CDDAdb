package cddadb.parser

import cddadb.parser.POParser.Trans

trait Parser {
  def fromText(text: String): this.type

  def fromFile(file: String): this.type

  def parse: List[Trans]

  val TEXT = 1
  val FILE = 2
}
