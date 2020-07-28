package manager

import java.io.File

import common.{Field, Type}
import play.api.Logger
import play.api.libs.json._
import utils.FileUtil._
import utils.JsonUtil._
import utils.StringUtil.parseColor

import scala.collection.mutable.ListBuffer

abstract class BaseResourceManager extends ResourceManager {
  protected val log: Logger = Logger(this.getClass)

  protected var poPath: String = _
  protected var dataPath: List[String] = _

  private[this] val trans = TransManager

  // typeId -> json obj
  // 不同type可能有相同id
  private[this] val typeIdToObj = Map[String, JsObject]()

  //copy-from obj cache
  private[this] var cpfCache = ListBuffer[JsObject]()

  override def update(): Unit = {
    dataPath.foreach {
      path =>
        val files = ls(new File(path), recursive = true, ONLY_FILE)
        files.foreach(fromFile(_).foreach(registerJsObj(_)))
    }
    // 处理cpfCache
    //FIXME: 可以将所有json预加载，然后根据id递归处理copy-from，O(n)时间
    // 下面这个循环最坏情况为 O(n^2)，如果继承关系在文件中正序，则为O(n)
    while (cpfCache.nonEmpty) {
      cpfCache = cpfCache.filter(!registerJsObj(_))
    }
  }

  // 这里根据目前支持的type进行逐个添加，更完善后直接exclude就行了
  private val blacklist = Set[String](
    Type.EFFECT_TYPE, Type.MIGRATION, Type.TALK_TOPIC,
    Type.OVERMAP_TERRAIN
  )

  private def registerJsObj(implicit jsObject: JsObject): Boolean = {
    val name = jsObject \ Field.NAME
    log.info(s"registering: $name")

    val tp = getField(Field.TYPE, jsObject, Type.NONE)(_.as[String]).toLowerCase

    try {
      if (blacklist.contains(tp)) {
        false // 不注册该类型json
      } else {
        preProcess(jsObject, tp) match {
          case Some(value) =>
            val (key, obj) = value
            var id = getString(Field.ABSTRACT)

            // abstract json 不进行postProcess
            if (id == "") {
              postProcess(key, obj)
              log.info(s"indexed: $key, json: $obj")
              id = getString(Field.ID)
            }

            typeIdToObj += s"$tp.$id" -> obj

            true
          // json继承失败
          case None => false
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(s"register fail, err: $e, obj: $jsObject")
        false
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   * 本方法返回的JsObject就是此后展示层接受的数据，所以任何字段处理都必须在此处理完
   *
   * @see [[Type]]
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

    // 处理color字段
    pend = handleColor(pend)

    // 生成索引key
    val key = indexKey(tp, getString(Field.NAME)(pend))

    import Type._
    import handler.HandlerImplicit._

    // 按需对各Type做进一步处理
    // 只做特异性处理
    tp match {
      case MONSTER => Some(key -> pend.process[Monster])
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
  private def handleCopyFrom(implicit jsObject: JsObject): Option[JsObject] = {
    // 处理relative和proportional
    def valueInherit(obj: JsObject, field: String) = {
      val valIhrFunc = (n: JsNumber, v: BigDecimal) => field match {
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
                    (toPath \ key).json.update(__.read[JsNumber].map(n => valIhrFunc(n, x.value)))
                  case JsError(_) =>
                    __.json.update((toPath \ key).json.put(valIhrFunc(JsNumber(0), x.value)))
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
              val v = fieldObj(key)
              pend = pend \ key match {
                case JsDefined(_) =>
                  val tf = field match {
                    case Field.EXTEND =>
                      // 如果出错就说明extend的字段在父json中可以以非数组形式存在（文档没说，遇到bug再改）
                      (__ \ key).json.update(__.read[JsArray].map(arr => arr ++ v.as[JsArray]))
                    case Field.DELETE =>
                      (__ \ key).json.prune
                  }
                  pend.transform(tf).get
                case JsUndefined() =>
                  if (field == Field.EXTEND) pend ++ Json.obj(key -> v)
                  else pend
              }
          }
          Some(pend - field)
        case JsError(_) => Some(pend)
      }
    }

    val tp = getString(Field.TYPE).toLowerCase
    val parId = getString(Field.COPY_FROM)
    val parent = typeIdToObj.get(s"$tp.$parId")
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
        log.info(s"copy-from handle failed, wait for another loop, parent: ${s"$tp.$parId"} json: $jsObject")
        None // parent还没加载，先缓存起来后续处理
    }
  }

  protected def indexKey(tp: String, name: String): String = s"$tp.$name"

  private def tranObj(jsObject: JsObject): JsObject = {
    val toTran = List(Field.NAME, Field.DESCRIPTION)
    var res = jsObject
    toTran.foreach {
      field =>
        res = tranField(res, field)
    }
    res
  }

  private def handleColor(obj: JsObject): JsObject = {
    val tf = (__ \ Field.COLOR).json.update(__.read[JsString].map(str => JsArray(parseColor(str.as[String]).map(JsString))))
    obj.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(_) => obj
    }
  }

  /**
   * field的翻译处理
   */
  private def tranField(implicit jsObject: JsObject, field: String): JsObject = {
    val tran = getField(field, jsObject, "") {
      case res: JsString =>
        val msgid = res.as[String]
        getTran(msgid, "")
      case res: JsObject =>
        var msgid = getString(Field.STR_SP)(res)
        msgid = getField(Field.STR, res, msgid)(_.as[String])
        // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
        // msgid = getField("str_pl", res, msgid)(_.as[String])
        val ctxt = getString(Field.CTXT)(res)
        getTran(msgid, ctxt)
      case res: JsArray =>
        val msgid = res.value(0).as[String]
        getTran(msgid, "")
      // case res: _ =>
      // log.warn(s"name format not supported, format: $res")
      // FIXME: trans to what?
    }

    val tf = __.json.update((__ \ field).json.put(JsString(tran)))
    jsObject.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(errors) =>
        log.info(errors.toString())
        jsObject
    }
  }

  def getTran(msg: String, ctxt: String): String = trans.get(msg, ctxt)

  //========================模板方法==================================
  /**
   * 供子类实现，子类将根据自身的索引维护方式注册索引
   *
   * @param key 索引key
   * @param jo  json对象
   */
  protected def postProcess(key: String, jo: JsObject): Unit
}