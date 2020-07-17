package cddadb.parser

import java.io.Reader

abstract class AbstractParser extends Parser {
  var text: String = ""
  var reader: Reader = _
  var mode: Int = 0

  override def fromText(text: String): Parser = {
    this.text = text
    setMode(TEXT)
    this
  }

  override def fromFile(file: String): Parser = {
    import cddadb.utils.FileUtil.classpathFileReader
    this.reader = classpathFileReader(file)
    setMode(FILE)
    this
  }

  def setMode(mode: Int): Unit = {
    this.mode = mode
  }

  def getMode: Int = mode
}
