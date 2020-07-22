package cddadb.manager

import java.io.File

import cddadb.common.{Field, Type}
import cddadb.parser.POParser
import cddadb.utils.JsonUtil._
import play.api.Logger
import play.api.libs.json._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class BaseResourceManager(poPath: String, dataPath: List[String])
  extends ResourceManager {

  private[this] val log = Logger(this.getClass)

  protected type Map[K, V] = mutable.Map[K, V]
  protected val Map: mutable.Map.type = mutable.Map

  private[this] val trans = Map[String, Map[String, String]]()

  // id -> json obj
  private[this] val idToObj = Map[String, JsObject]()

  //copy-from obj cache
  private[this] var cpfCache = ListBuffer[JsObject]()

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
    dataPath.foreach {
      path =>
        val files = ls(new File(path), recursive = true, ONLY_FILE)
        files.foreach(fromFile(_).foreach(registerJsObj))
    }
    // 处理cpfCache
    //FIXME: 可以将所有json预加载，然后根据id递归处理copy-from，O(n)时间
    // 下面这个循环最坏情况为 O(n^2)，如果继承关系在文件中正序，则为O(n)
    while (cpfCache.nonEmpty) {
      cpfCache = cpfCache.filter(!registerJsObj(_))
    }
  }

  // 这里根据目前支持的type进行逐个添加，更完善后直接exclude就行了
  private val include = Set(
    Type.MONSTER, Type.AMMO, Type.COMESTIBLES, Type.BOOK
  )

  private def registerJsObj(jsObject: JsObject): Boolean = {
    log.info(s"Registering ${jsObject \ Field.NAME}")
    val tp = getField(Field.TYPE, jsObject, Type.NONE)(_.as[String])
    if (include contains tp) {
      preProcess(jsObject, tp) match {
        case Some(value) =>
          val (key, obj) = value
          var id = getStringField(Field.ABSTRACT, jsObject)
          // abstract json 不进行postProcess
          if (id == "") {
            postProcess(key, obj)
            log.info(s"indexed: $key, json: $obj")
            id = getStringField(Field.ID, jsObject)
          }
          idToObj += id -> obj
          true
        case None => false
      }
    } else {
      false
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   *
   * @see [[cddadb.common.Type]]
   */
  private def preProcess(jsObject: JsObject, tp: String): Option[(String, JsObject)] = {
    var pend = jsObject

    // 处理json继承
    if (hasField(Field.COPY_FROM, jsObject)) {
      handleCopyFrom(jsObject) match {
        case Some(value) => pend = value
        case None => return None
      }
    }

    // 翻译json
    //FIXME: 如果子json继承description字段，将导致重复翻译
    pend = tranObj(pend)

    val key = indexKey(tp, getStringField(Field.NAME, pend))
    // 针对json类型进一步处理
    tp match {
      case Type.MONSTER => Some(key -> processMonster(pend))
      case Type.AMMO => Some(key -> processAmmo(pend))
      case _ => Some(key -> pend)
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

      def handleObj(path: JsPath, toPath: JsPath, o: JsObject): JsObject = {
        var pend = o
        val t = o.transform(path.json.pick[JsObject]).get
        t.keys.foreach {
          key =>
            t(key) match {
              case _: JsObject =>
                pend = handleObj(path \ key, toPath \ key, pend)
              case x: JsNumber =>
                val tf = pend.transform((toPath \ key).json.pick[JsNumber]) match {
                  case JsSuccess(_, _) =>
                    (toPath \ key).json.update(__.read[JsNumber].map(n => func(n, x.value)))
                  case JsError(_) =>
                    __.json.update((toPath \ key).json.put(func(JsNumber(0), x.value)))
                }
                pend.transform(tf) match {
                  case JsSuccess(value, _) => pend = value
                  case JsError(err) => log.info(err.toString())
                }
              case _ =>
            }
        }
        pend
      }

      val path = __ \ field
      obj.transform(path.json.pick[JsObject]) match {
        case JsSuccess(_, _) =>
          Some(handleObj(path, __, obj) - field)
        case JsError(_) =>
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
                case x: JsArray => field match {
                  case Field.EXTEND => (__ \ key).json.update(__.read[JsArray].map(n => n ++ x))
                  case Field.DELETE => (__ \ key).json.update(__.read[JsArray].map(n => JsArray(n.value.filter(!x.value.contains(_)))))
                }
                case x => field match {
                  case Field.EXTEND => (__ \ key).json.update(__.read[JsObject].map(_ => x))
                  case Field.DELETE => (__ \ key).json.prune
                }
              }
              pend = pend.transform(tf).get
          }
          Some(pend - field)
        case JsError(_) =>
          Some(obj)
      }
    }

    val parId = getStringField(Field.COPY_FROM, jsObject)
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

  private def indexKey(tp: String, name: String): String =
    s"$tp.$name"

  private def tranObj(jsObject: JsObject): JsObject = {
    val toTran = List(Field.NAME, Field.DESCRIPTION)
    var res = jsObject
    toTran.foreach {
      field =>
        res = tranField(res, field)
    }
    res
  }

  /**
   * field的翻译处理
   */
  private def tranField(jsObject: JsObject, field: String): JsObject = {
    val tran = field match {
      case Field.NAME =>
        getField(Field.NAME, jsObject, "") {
          case res: JsString =>
            val msgid = res.as[String]
            getTran(msgid, "")
          case res: JsObject =>
            var msgid = getStringField(Field.STR_SP, res)
            msgid = getField(Field.STR, res, msgid)(_.as[String])
            // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
            // msgid = getField("str_pl", res, msgid)(_.as[String])
            val ctxt = getStringField(Field.CTXT, res)
            getTran(msgid, ctxt)
        }
      case _ =>
        getTran(getStringField(Field.DESCRIPTION, jsObject), "")
    }
    val tf = __.json.update((__ \ field).json.put(JsString(tran)))
    jsObject.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(errors) =>
        log.info(errors.toString())
        jsObject
    }
  }

  def getTran(msg: String, ctxt: String): String = {
    trans.get(msg).flatMap(_.get(ctxt)).getOrElse(msg)
  }


  //======================各类型处理方法===================================
  //针对每一个特定的json类型，可对json字段进行增删改
  //返回此json对象的索引key，和处理后的json对象
  //====================================================================

  private def processMonster(jsObject: JsObject): JsObject = {
    // may be do something
    jsObject
  }

  private def processAmmo(jsObject: JsObject): JsObject = {
    // may be do something
    jsObject
  }
}
