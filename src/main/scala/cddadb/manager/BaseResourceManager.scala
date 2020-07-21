package cddadb.manager

import java.io.File

import cddadb.common.{Field, Type}
import cddadb.parser.POParser
import cddadb.utils.JsonUtil._
import play.api.libs.json._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class BaseResourceManager(poPath: String, dataPath: String)
  extends ResourceManager {

  protected type Map[K, V] = mutable.Map[K, V]
  protected val Map: mutable.Map.type = mutable.Map

  private[this] val trans = Map[String, Map[String, String]]()

  // id -> json obj
  private[this] val idToObj = Map[String, JsObject]()

  //copy-from obj cache
  private[this] val cpfCache = ListBuffer[JsObject]()

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

  protected def loadDataFiles(): Unit = {
    import cddadb.utils.FileUtil._
    import cddadb.utils.JsonUtil._
    val files = ls(new File(dataPath), recursive = true, ONLY_FILE)
    files.foreach(fromFile(_).foreach(registerJsObj))
  }

  private def registerJsObj(jsObject: JsObject): Unit = {
    preProcess(jsObject) match {
      case Some(value) =>
        val (key, obj) = value
        var id = getField(Field.ABSTRACT, jsObject, "")(_.as[String])
        if (id == "") {
          postProcess(key, obj)
          id = getField(Field.ID, jsObject, "")(_.as[String])
        }
        idToObj += id -> obj
      case None =>
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   *
   * @see [[cddadb.common.Type]]
   */
  private def preProcess(jsObject: JsObject): Option[(String, JsObject)] = {
    var pend = jsObject
    val tp = getField("type", jsObject, Type.NONE)(_.as[String])

    if (hasField(Field.COPY_FROM, jsObject)) {
      handleCopyFrom(jsObject) match {
        case Some(value) => pend = value
        case None => return None
      }
    }

    tp match {
      case Type.MONSTER => Some(processMonster(pend))
      case Type.AMMO => Some(processAmmo(pend))
      case _ => None
    }
  }

  /**
   * 处理继承自其他json的json
   * 继承规则：https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INHERITANCE.md
   *
   * @param jsObject
   * @return 返回一个继承父json后的json对象
   */
  private def handleCopyFrom(jsObject: JsObject): Option[JsObject] = {
    // 处理relative和proportional
    def valueInherit(obj: JsObject, field: String) = {
      val func = (n: JsNumber, v: BigDecimal) => field match {
        case Field.RELATIVE => JsNumber(n.value + v)
        case Field.PROPORTIONAL => JsNumber(n.value * v)
      }

      def handleObj(path: JsPath, o: JsObject): JsObject = {
        var pend = o
        o.keys.foreach {
          key =>
            o(key) match {
              case x: JsObject =>
                pend = handleObj(path \ key, x)
              case JsNumber(value) =>
                val tf = (path \ key).json.update(__.read[JsNumber].map(n => func(n, value)))
                pend = obj.transform(tf).get
              case _ =>
            }
        }
        pend
      }

      val path = __ \ field
      obj.transform(path.json.pick[JsObject]) match {
        case JsSuccess(value, _) =>
          Some(handleObj(path, value) - field)
        case JsError(err) =>
          Some(obj)
      }
    }

    // 处理extend和delete
    def fieldInherit(obj: JsObject, field: String) = {
      var pend = obj
      pend.transform((__ \ field).json.pick[JsObject]) match {
        case JsSuccess(fieldObj, _) =>
          fieldObj.keys.foreach {
            key =>
              val tf = fieldObj(key) match {
                case x: JsObject => field match {
                  case Field.EXTEND => (__ \ key).json.update(__.read[JsObject].map(_ => x))
                  case Field.DELETE => (__ \ key).json.prune
                }
                case x: JsArray => field match {
                  case Field.EXTEND => (__ \ key).json.update(__.read[JsArray].map(n => n ++ x))
                  case Field.DELETE => (__ \ key).json.update(__.read[JsArray].map(n => JsArray(n.value.filter(!x.value.contains(_)))))
                }
              }
              pend = pend.transform(tf).get
          }
          Some(pend - field)
        case JsError(err) =>
          Some(obj)
      }
    }

    val parId = getField(Field.COPY_FROM, jsObject, "")(_.as[String])
    val parent = idToObj.get(parId)
    parent match {
      case Some(p) =>
        val newObj = p ++ jsObject - Field.COPY_FROM - Field.ABSTRACT
        for {
          i <- valueInherit(newObj, Field.RELATIVE)
          j <- valueInherit(i, Field.PROPORTIONAL)
          k <- fieldInherit(j, Field.EXTEND)
          l <- fieldInherit(k, Field.DELETE)
        } yield l
      case None =>
        cpfCache += jsObject
        None // parent还没加载，先缓存起来后续处理
    }
  }

  /**
   * 模板方法，供子类实现，子类将根据自身的索引维护方式注册索引
   *
   * @param key 索引key
   * @param jo  json对象
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
    // may be do something
    key -> jsObject
  }

  private def processAmmo(jsObject: JsObject): (String, JsObject) = {
    val key = indexKey(Type.AMMO, name(jsObject))

    key -> jsObject
  }

  /**
   * name field的翻译处理
   */
  private def name(jsObject: JsObject): String = {
    getField(Field.NAME, jsObject, "") {
      case res: JsString =>
        val msgid = res.as[String]
        tran(msgid, "")
      case res: JsObject =>
        var msgid = getField(Field.STR_SP, res, "")(_.as[String])
        msgid = getField(Field.STR, res, msgid)(_.as[String])
        // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
        // msgid = getField("str_pl", res, msgid)(_.as[String])
        val ctxt = getField(Field.CTXT, res, "")(_.as[String])
        tran(msgid, ctxt)
    }
  }

  private def indexKey(tp: String, name: String): String =
    s"$tp.$name"

  def tran(msg: String, ctxt: String): String =
    trans.get(msg).flatMap(_.get(ctxt)).getOrElse(msg)
}
