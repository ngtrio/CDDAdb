package cddadb.parser

import cddadb.parser.POParser.Trans

trait Parser {
  def fromText(text: String): Parser

  def fromFile(file: String): Parser

  def parse: List[Trans]

  val TEXT = 1
  val FILE = 2
}
