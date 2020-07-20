package cddadb.manager

import java.io.File

import cddadb.common.Type
import cddadb.parser.POParser
import cddadb.utils.JsonUtil._
import play.api.libs.json.{JsObject, JsString}

import scala.collection.mutable

abstract class BaseResourceManager(poPath: String, dataPath: String)
  extends ResourceManager {

  protected type Map[K, V] = mutable.Map[K, V]
  protected val Map: mutable.Map.type = mutable.Map

  private[this] val trans = Map[String, Map[String, String]]()

  protected def updateTrans(): Unit = {
    val poParser = POParser()
    val res = poParser.fromFile(poPath).parse
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
  }

  def tran(msg: String, ctxt: String): String = {
    trans.get(msg).flatMap(_.get(ctxt)).getOrElse(msg)
  }

  protected def loadDataFiles(): Unit = {
    import cddadb.utils.FileUtil._
    import cddadb.utils.JsonUtil._
    val files = ls(new File(dataPath), recursive = true, ONLY_FILE)
    files.foreach(fromFile(_).foreach(registerJsObj))
  }

  private def registerJsObj(jsObject: JsObject): Unit = {
    preProcess(jsObject) match {
      case Some(value) =>
        val (key, jo) = value
        postProcess(key, jo)
      case None =>
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   *
   * @see [[cddadb.common.Type]]
   */
  private def preProcess(jsObject: JsObject): Option[(String, JsObject)] = {
    if (!hasField("copy-from", jsObject)) {
      val tp = getField("type", jsObject, Type.NONE)(_.as[String])
      tp match {
        case Type.MONSTER => Some(processMonster(jsObject))
        case _ => None
      }
    } else {
      // TODO: copyfrom处理
      None
    }
  }

  /**
   * 处理继承自其他json的json
   * 继承规则：https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INHERITANCE.md
   *
   * @param jsObject
   */
  private def handleCopyFrom(jsObject: JsObject) = {
  }

  /**
   * 模板方法，供子类实现，子类将根据自身的索引维护方式注册索引
   *
   * @param key 索引key
   * @param jo  json 对象
   */
  protected def postProcess(key: String, jo: JsObject): Unit

  /**
   * TODO: 处理MONSTER类型的json
   *
   * @param jsObject
   * @return (index key, json object)
   */
  private def processMonster(jsObject: JsObject): (String, JsObject) = {
    val key = indexKey(Type.MONSTER, name(jsObject))
    key -> jsObject
  }

  /**
   * name field的翻译处理
   */
  private def name(jsObject: JsObject): String = {
    getField("name", jsObject, "") {
      case res: JsString =>
        val msgid = res.as[String]
        tran(msgid, "")
      case res: JsObject =>
        var msgid = getField("str_sp", res, "")(_.as[String])
        msgid = getField("str", res, msgid)(_.as[String])
        // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
        //        msgid = getField("str_pl", res, msgid)(_.as[String])
        val ctxt = getField("ctxt", res, "")(_.as[String])
        tran(msgid, ctxt)
    }
  }

  private def indexKey(tp: String, name: String): String =
    s"$tp.$name"
}
