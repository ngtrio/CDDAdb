package manager

import parser.POParser

object TransManager extends Manager {
  private val trans = Map[String, Map[String, String]]()
  private val poParser = POParser()
  private val res = poParser.fromFile("data/zh.po").parse

  res.foreach {
    case POParser.SingleTrans(msgctxt, msgid, msgstr) =>
      trans.get(msgid) match {
        case Some(v) => v += msgctxt -> msgstr
        case None => trans += msgid -> Map(msgctxt -> msgstr)
      }
    case POParser.PluralTrans(msgctxt, msgid, _, msgstr) =>
      trans.get(msgid) match {
        case Some(v) => v += msgctxt -> msgstr.head._2
        case None => trans += msgid -> Map(msgctxt -> msgstr.head._2)
      }
  }

  def get(toTran: String, ctxt: String = ""): String = {
    trans.get(toTran).flatMap(_.get(ctxt)).getOrElse(toTran)
  }
}