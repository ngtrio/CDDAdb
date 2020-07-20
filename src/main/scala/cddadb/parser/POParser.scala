package cddadb.parser

import scala.collection.mutable.ListBuffer
import scala.util.parsing.combinator.JavaTokenParsers


/**
 * .po file
 * comment ::= ("#"STRING)*
 * msgid ::= "msgid""\""STRING*"\""
 * msgstr ::= "msgstr""\""STRING*"\""
 * msgctxt ::= "msgctxt""\""STRING*"\""
 * msgid_plural ::= "msgid_plural""\""STRING*"\""
 * msgstr_idxed ::= "msgstr["INTEGER"]"
 *
 * TODO 注释类型解析
 */

object POParser {
  def apply(): Parser = new POParser

  sealed trait Trans

  case class SingleTrans(msgctxt: String, msgid: String,
                         msgstr: String) extends Trans

  case class PluralTrans(msgctxt: String, msgid: String, msgidP: String,
                         msgstr: List[(Int, String)]) extends Trans

}

class POParser extends AbstractParser with JavaTokenParsers {
  private def merge(strings: List[String]): String = {
    var res = ""
    strings.foreach {
      string =>
        // TODO: 更多的转义字符替换
        val str = string.substring(1, string.length - 1).
          replaceAll("""\\"""", "\"")
        res += str
    }
    res
  }

  private def comment: Parser[String] = rep("^#.*".r) ^^ merge

  private def msgid: Parser[String] = "msgid" ~ rep(stringLiteral) ^^ {
    case _ ~ strings => merge(strings)
  }

  private def msgstr: Parser[String] = "msgstr" ~ rep(stringLiteral) ^^ {
    case _ ~ strings => merge(strings)
  }

  private def msgctxt: Parser[String] = "msgctxt" ~ rep(stringLiteral) ^^ {
    case _ ~ strings => merge(strings)
  }

  private def msgidPlural: Parser[String] = "msgid_plural" ~ rep(stringLiteral) ^^ {
    case _ ~ strings => merge(strings)
  }

  private def msgstrIdxed: Parser[List[(Int, String)]] =
    rep("msgstr[" ~ decimalNumber ~ "]" ~ rep("^\".*\"".r)) ^^ {
      res =>
        val list = ListBuffer[(Int, String)]()
        res.foreach {
          case _ ~ num ~ _ ~ strings =>
            list += num.toInt -> merge(strings)
        }
        list.toList
    }

  import POParser._

  private def single: Parser[Trans] =
    opt(comment) ~ opt(msgctxt) ~
      opt(comment) ~ msgid ~
      opt(comment) ~ msgstr ~ opt(comment) ^^ {
      case _ ~ mc ~ _ ~ mi ~ _ ~ ms ~ _ =>
        mc match {
          case Some(value) =>
            SingleTrans(value, mi, ms)
          case None =>
            SingleTrans("", mi, ms)
        }
    }

  private def plural: Parser[Trans] =
    opt(comment) ~ opt(msgctxt) ~
      opt(comment) ~ msgid ~
      opt(comment) ~ msgidPlural ~
      opt(comment) ~ msgstrIdxed ~ opt(comment) ^^ {
      case _ ~ mc ~ _ ~ mi ~ _ ~ mip ~ _ ~ msi ~ _ =>
        mc match {
          case Some(value) =>
            PluralTrans(value, mi, mip, msi)
          case None =>
            PluralTrans("", mi, mip, msi)
        }
    }

  private def expr: Parser[List[Trans]] = rep(single | plural)

  override def parse: List[Trans] = {
    var parseResult: ParseResult[List[Trans]] = null
    parseResult = getMode match {
      case TEXT =>
        parseAll(expr, text)
      case FILE =>
        parseAll(expr, reader)
      case _ =>
        throw new Exception("no source is defined, see fromText or fromFile method")
    }
    parseResult match {
      case Success(result, _) =>
        result
      case Failure(msg, _) =>
        throw new Exception(msg)
    }
  }
}
