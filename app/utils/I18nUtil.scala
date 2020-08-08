package utils

import common.{Field, Type}
import handler.HandlerContext
import parser.POParser
import play.api.Logger
import play.api.libs.json._
import utils.JsonUtil._

import scala.collection.mutable

object I18nUtil {
  private val log = Logger(this.getClass)

  private val trans = mutable.Map[String, mutable.Map[String, String]]()
  private val poParser = POParser()
  log.info("loading translation file for the first time, wait...")
  private val res = poParser.fromFile("data/zh.po").parse

  res.foreach {
    case POParser.SingleTrans(msgctxt, msgid, msgstr) =>
      trans.get(msgid) match {
        case Some(v) => v += msgctxt -> msgstr
        case None => trans += msgid -> mutable.Map(msgctxt -> msgstr)
      }
    case POParser.PluralTrans(msgctxt, msgid, _, msgstr) =>
      trans.get(msgid) match {
        case Some(v) => v += msgctxt -> msgstr.head._2
        case None => trans += msgid -> mutable.Map(msgctxt -> msgstr.head._2)
      }
  }

  def tranObj(jsObject: JsObject, toTran: String*)
             (implicit hCtxt: HandlerContext = new HandlerContext()): JsObject = {
    var res: JsObject = jsObject
    toTran.foreach {
      field =>
        jsObject \ field match {
          case JsDefined(value) =>
            try {
              res ++= Json.obj(field -> tranField(field, value))
            } catch {
              case ex: Exception =>
                log.error(s"$ex, json: $jsObject")
                throw ex
            }
          // if field not found, just ignore
          case JsUndefined() => log.debug(s"field: $field not found in $jsObject")
        }
    }
    res
  }

  private def tranField(field: String, jsValue: JsValue)(implicit hCtxt: HandlerContext): JsValue = {
    try {
      field match {
        case Field.NAME => tranName(jsValue)
        case Field.DESCRIPTION => tranDescription(jsValue)
        case Field.RESULT => tranIdent(Type.ITEM, jsValue.as[String])
        case Field.QUALITIES => tranQualities(jsValue)
        case Field.TOOLS => tranTools(jsValue)
        case Field.COMPONENTS => tranComponent(jsValue)
        case Field.CRAFT_TO => tranCraftTo(jsValue)
        case Field.BOOK_LEARN => tranBookLearn(jsValue)
        case Field.RECIPES => tranRecipes(jsValue)
      }
    } catch {
      case err: Exception =>
        log.error(s"field: $field format error, json: $jsValue")
        throw err
    }
  }

  def tranName(jsValue: JsValue): JsString = {
    jsValue match {
      case res: JsString =>
        // 直接翻译string
        tranString(res.value)
      case res: JsObject =>
        // 存在复数形式的翻译
        var msgid = getString(Field.STR_SP)(res)
        msgid = getField(Field.STR, res, msgid)(_.as[String])
        // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
        // msgid = getField("str_pl", res, msgid)(_.as[String])
        val ctxt = getString(Field.CTXT)(res)
        tranString(msgid, ctxt)
      case res: JsValue =>
        throw new Exception(s"translate format not supported, format: $res")
    }
  }

  private def tranDescription(jsValue: JsValue): JsString = {
    val str = jsValue match {
      case res: JsString => res.value
      case res: JsObject => (res \ Field.STR).get.as[String]
      case _ => throw new Exception(s"invalid description format json: $jsValue")
    }
    tranString(str)
  }

  // [ { "id": "HAMMER_FINE", "level": 1 }, { "id": "SAW_M_FINE", "level": 1 }, { "id": "SCREW_FINE", "level": 1 } ]
  private def tranQualities(jsValue: JsValue)(implicit hCtxt: HandlerContext): JsArray = {
    var res = JsArray()
    val arr = jsValue.as[JsArray].value
    arr.foreach {
      obj =>
        val ident = getString(Field.ID)(obj)
        val level = getNumber(Field.LEVEL)(obj)
        val name = tranIdent(Type.TOOL_QUALITY, ident)
        res :+= Json.obj(
          Field.ID -> ident,
          Field.NAME -> name,
          Field.LEVEL -> level
        )
    }
    res
  }

  private def tranTools(jsValue: JsValue)(implicit hCtxt: HandlerContext): JsArray = {
    tranComponent(jsValue)
  }

  private def tranComponent(jsValue: JsValue)(implicit hCtxt: HandlerContext): JsArray = {
    val arr = jsValue.as[JsArray].value
    var newComponents = JsArray()
    arr.foreach {
      alt =>
        var newAlt = JsArray()
        alt.as[JsArray].value.foreach {
          igre =>
            val arr = igre.as[JsArray].value
            val ident = arr(0).as[String]
            val amount = arr(1).as[Int]
            newAlt :+= Json.arr(ident, tranIdent(Type.ITEM, ident), amount)
        }
        newComponents :+= newAlt
    }
    newComponents
  }

  private def tranCraftTo(jsValue: JsValue)(implicit hCtxt: HandlerContext): JsArray = {
    val ct = jsValue.as[JsArray]
    ct.value.foldLeft(JsArray()) {
      (res, id) =>
        val name = tranIdent(Type.ITEM, id.as[String])
        res :+ Json.arr(id, name)
    }
  }

  private def tranBookLearn(jsValue: JsValue)(implicit hCtxt: HandlerContext): JsArray = {
    val ct = jsValue.as[JsArray]
    ct.value.foldLeft(JsArray()) {
      (res, book) =>
        val bookId = book(0)
        val name = tranIdent(Type.ITEM, bookId.as[String])
        // if level does not exist, set to 0
        val lv = if (book.as[JsArray].value.length > 1) book(1).as[Int] else 0
        res :+ Json.arr(bookId, name, lv)
    }
  }

  private def tranRecipes(jsValue: JsValue)(implicit hctxt: HandlerContext): JsArray = {
    jsValue.as[JsArray].value.foldLeft(JsArray()) {
      (res, jVal) =>
        val arr = jVal.as[JsArray]
        val rpId = arr(0).as[String]
        val rpName = arr(1).as[String]
        val finalName = if (rpName == "") tranIdent(Type.ITEM, rpId) else JsString(rpName)
        res :+ Json.arr(rpId, finalName)
    }
  }

  // 将field to的翻译映射到id上
  def tranIdent(tp: String, ident: String, to: String = Field.NAME)
               (implicit hCtxt: HandlerContext): JsString = {
    hCtxt.objCache(tp).get(ident) match {
      case Some(value) =>
        val jsValue = value \ to match {
          case JsDefined(value) => value
          case JsUndefined() => throw new Exception(s"field $to not found in $ident")
        }
        to match {
          case Field.NAME => tranName(jsValue)
          case _ => tranString(jsValue.as[String])
        }
      case None =>
        log.warn(s"obj of type: $tp, ident: $ident not found")
        JsString(ident)
    }
  }

  def tranString(toTran: String, ctxt: String = ""): JsString = {
    JsString(trans.get(toTran).flatMap(_.get(ctxt)).getOrElse(toTran))
  }
}
