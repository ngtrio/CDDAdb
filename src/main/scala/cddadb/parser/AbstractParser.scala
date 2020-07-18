package cddadb.parser

import java.io.{FileReader, Reader}

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
    import cddadb.utils.FileUtil.workDirFile
    this.reader = new FileReader(workDirFile(file))
    setMode(FILE)
    this
  }

  def setMode(mode: Int): Unit = {
    this.mode = mode
  }

  def getMode: Int = mode
}
