package manager

import common.Field
import common.Field._
import common.Type.{AMMO, MONSTER}
import handler.HandlerChain
import play.api.Logger
import play.api.libs.json._
import utils.JsonUtil
import utils.JsonUtil.getString

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * @author jaron
 *         created on 2020/12/27 at 下午10:51
 */
class Mod(val meta: ModMeta) {

  private val log = Logger(this.getClass)

  private val _normal = mutable.Map[String, mutable.Map[String, JsObject]]()
  private var _pendingCopyFrom = mutable.Map[String, mutable.Map[String, JsObject]]()
  private var _doingCopyFrom = mutable.Map[String, mutable.Map[String, JsObject]]()

  def addNormal(`type`: String, id: String, json: JsObject): Unit = {
    _normal.getOrElseUpdate(`type`, mutable.Map[String, JsObject]()) += id -> json
  }

  def addPendingCopyFrom(`type`: String, id: String, json: JsObject): Unit = {
    _pendingCopyFrom.getOrElseUpdate(`type`, mutable.Map[String, JsObject]()) += id -> json
  }

  def search(`type`: String, id: String): Option[JsObject] = {
    _normal.get(`type`).flatMap(_.get(id))
  }

  def loadJson(implicit json: JsObject): Unit = {
    if (shouldLoad(json)) {
      val `type` = JsonUtil.getString(Field.TYPE).toLowerCase
      if (`type` == "") {
        log.warn(s"Discard json: no type field, $json")
      } else {
        val idents = JsonUtil.getIdent(`type`)
        val parId = JsonUtil.getString(Field.COPY_FROM)
        if (parId == "") {
          idents.foreach(addNormal(`type`, _, json))
        } else {
          idents.foreach(addPendingCopyFrom(`type`, _, json))
        }
      }
    }
  }

  private def shouldLoad(jsObject: JsObject): Boolean = {
    val whiteList = List(MONSTER, AMMO)
    val tp = getString(TYPE)(jsObject).toLowerCase

    val isObsolete: Boolean = {
      jsObject \ Field.OBSOLETE match {
        case JsDefined(value) => value.as[Boolean]
        case JsUndefined() => false
      }
    }

    val isWhite: Boolean = {
      whiteList.contains(tp)
    }

    !isObsolete && isWhite
  }

  def handleCopyFrom(dependencies: List[Mod]): Unit = {
    if (_pendingCopyFrom != null && _doingCopyFrom != null) {
      _pendingCopyFrom.foreach {
        pair =>
          val `type` = pair._1
          pair._2.keys.foreach(doHandleCopyFrom(dependencies, `type`, _))
          log.info(s"[Mod]: ${meta.name} => ${`type`} loaded, total ${_normal(`type`).size}")
      }
    }
    _pendingCopyFrom = null
    _doingCopyFrom = null
  }

  private def doHandleCopyFrom(dependencies: List[Mod], `type`: String, id: String): Option[JsObject] = {

    // 处理relative和proportional
    def valueInherit(obj: JsObject, field: String) = {

      val valIhrFunc = (n: JsNumber, v: BigDecimal) =>
        field match {
          case RELATIVE => JsNumber(n.value + v)
          case PROPORTIONAL => JsNumber(n.value * v)
        }

      def handleObj(path: JsPath, toPath: JsPath, o: JsObject): JsObject = {
        var pend = o
        val t = o.transform(path.json.pick[JsObject]).get
        t.keys.foreach { key =>
          t(key) match {
            case _: JsObject =>
              pend = handleObj(path \ key, toPath \ key, pend)
            case x: JsNumber =>
              val tf =
                pend.transform((toPath \ key).json.pick[JsNumber]) match {
                  case JsSuccess(_, _) =>
                    (toPath \ key).json.update(
                      __.read[JsNumber].map(n => valIhrFunc(n, x.value))
                    )
                  case JsError(_) =>
                    __.json.update(
                      (toPath \ key).json.put(valIhrFunc(JsNumber(0), x.value))
                    )
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
        case JsError(_) => Some(obj)
      }
    }

    // 处理extend和delete
    def fieldInherit(obj: JsObject, field: String) = {
      var pend = obj
      pend.transform((__ \ field).json.pick[JsObject]) match {
        case JsSuccess(fieldObj, _) =>
          fieldObj.keys.foreach { key =>
            val v = fieldObj(key)
            pend = pend \ key match {
              case JsDefined(_) =>
                val tf = field match {
                  case EXTEND =>
                    // 如果出错就说明extend的字段在父json中可以以非数组形式存在（文档没说，遇到bug再改）
                    (__ \ key).json.update(
                      __.read[JsArray].map(arr => arr ++ v.as[JsArray])
                    )
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


    if (containsJson(_pendingCopyFrom, `type`, id)) {
      val json = _pendingCopyFrom(`type`)(id)
      val parId = JsonUtil.getString(Field.COPY_FROM)(json)
      _doingCopyFrom.getOrElseUpdate(`type`, mutable.Map[String, JsObject]()) += id -> json
      _pendingCopyFrom(`type`).remove(id)

      val parObj = doHandleCopyFrom(dependencies, `type`, parId)

      parObj match {
        case Some(parent) =>
          val newObj = parent ++ json - COPY_FROM - ABSTRACT
          val res = for {
            i <- valueInherit(newObj, RELATIVE)
            j <- valueInherit(i, PROPORTIONAL)
            k <- fieldInherit(j, EXTEND)
            l <- fieldInherit(k, DELETE)
          } yield l
          res match {
            case Some(value) =>
              _normal.getOrElseUpdate(`type`, mutable.Map[String, JsObject]()) += id -> value
              _doingCopyFrom(`type`).remove(id)
              log.debug(s"copy-from handle success, json: ${res.get}")
              Some(value)
            case None =>
              log.warn(s"For $json, parent: $parId is loaded, but copy-from failed")
              None
          }
        case None =>
          if (containsJson(_doingCopyFrom, `type`, parId)) {
            // 循环依赖
            log.warn(s"Circular dependency found, id1: $id, id2: $parId")
          } else {
            // 资源文件出错
            log.warn(s"For $json, parent: $parId is required, but load failed")
          }
          None
      }
    } else if (containsJson(_normal, `type`, id)) {
      Some(_normal(`type`)(id))
    } else {
      findFromDependencies(dependencies, `type`, id)
    }
  }

  private def containsJson(cache: mutable.Map[String, mutable.Map[String, JsObject]],
                           `type`: String, id: String): Boolean = {
    cache.contains(`type`) && cache(`type`).contains(id)
  }

  private def findFromDependencies(dependencies: List[Mod], `type`: String, id: String): Option[JsObject] = {
    var res: Option[JsObject] = None
    var i = 0
    while (res.isEmpty && i < dependencies.length) {
      res = dependencies(i).search(`type`, id)
      i += 1
    }
    res
  }

  def processJson(): Unit = {
    val chain = HandlerChain()
    _normal.keys.foreach {
      `type` =>
        _normal(`type`).keys.foreach {
          id => _normal(`type`)(id) = chain.handle(_normal(`type`)(id))
        }
    }
  }
}
