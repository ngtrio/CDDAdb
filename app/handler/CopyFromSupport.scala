package handler

import common.Field._
import play.api.Logger
import play.api.libs.json._
import utils.JsonUtil._

import scala.collection.mutable

trait CopyFromSupport {
  protected val log: Logger = Logger(this.getClass)

  //FIXME： 释放此处内存
  protected val objCache: mutable.Map[String, JsObject] = mutable.Map[String, JsObject]()

  /**
   * 处理json inheritance
   * 继承规则：https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INHERITANCE.md
   *
   * @param ident 该类型的唯一标识
   * @return copy-from处理不成功或者为abstract obj则返回None，否则返回处理成功后的obj
   */
  protected def handleCopyFrom(implicit jsObject: JsObject, ident: String): Option[JsObject] = {

    // 处理relative和proportional
    def valueInherit(obj: JsObject, field: String) = {
      val valIhrFunc = (n: JsNumber, v: BigDecimal) => field match {
        case RELATIVE => JsNumber(n.value + v)
        case PROPORTIONAL => JsNumber(n.value * v)
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
                    case EXTEND =>
                      // 如果出错就说明extend的字段在父json中可以以非数组形式存在（文档没说，遇到bug再改）
                      (__ \ key).json.update(__.read[JsArray].map(arr => arr ++ v.as[JsArray]))
                    case DELETE =>
                      (__ \ key).json.prune
                  }
                  pend.transform(tf).get
                case JsUndefined() =>
                  if (field == EXTEND) pend ++ Json.obj(key -> v)
                  else pend
              }
          }
          Some(pend - field)
        case JsError(_) => Some(pend)
      }
    }

    val id = getField(ABSTRACT, jsObject, ident)(_.as[String])

    val parId = getString(COPY_FROM)
    if (parId == "") {
      objCache += id -> jsObject
      Some(jsObject)
    } else {
      val parent = objCache.get(s"$parId")
      parent match {
        case Some(p) =>
          val newObj = p ++ jsObject - COPY_FROM - ABSTRACT
          val res = for {
            i <- valueInherit(newObj, RELATIVE)
            j <- valueInherit(i, PROPORTIONAL)
            k <- fieldInherit(j, EXTEND)
            l <- fieldInherit(k, DELETE)
          } yield l
          objCache += id -> res.get // copy-from成功，缓存起来以供后续继承
          log.debug(s"copy-from handle success, json: ${res.get}")
          res
        case None =>
          log.debug(s"copy-from handle fail, wait for another loop, parent: $parId, json: $jsObject")
          None
      }
    }
  }
}
