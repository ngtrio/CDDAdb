package manager

import java.io.File

import common.Field._
import common.Type._
import handler._
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.{Configuration, Logger}
import utils.FileUtil._
import utils.JsonUtil._

import scala.collection.mutable

@Singleton
class BaseResourceManager extends ResourceManager {
  private val log: Logger = Logger(this.getClass)

  private var poPath: String = _
  private var dataPath: List[String] = _
  implicit private val handlerCtxt: HandlerContext = new HandlerContext()

  @Inject
  def this(config: Configuration) = {
    this()
    this.poPath = config.get[String]("poPath")
    this.dataPath = config.get[Seq[String]]("dataPath").toList
  }

  // for direct usage
  def this(poPath: String, dataPath: List[String]) = {
    this()
    this.poPath = poPath
    this.dataPath = dataPath
  }

  override def update(): List[(String, JsValue)] = {
    // update resource files
    // retries at most 3 times for bad network
    val retries = 3
    var flag = false
    for (_ <- 1 to retries; if !flag)
      flag = ResourceUpdater.update()

    try {
      dataPath.foreach {
        path =>
          val files = ls(new File(path), recursive = true, ONLY_FILE)
          files.foreach(fromFile(_).foreach(loadJson))
      }
      copyFrom()
      postProcess()
      handlerCtxt.indexed.toList
    } finally {
      clear()
    }
  }

  private def clear(): Unit = {
    handlerCtxt.clear()
  }

  private def shouldLoad(jsObject: JsObject): Boolean = {
    val blacklist = List(EFFECT_TYPE, TALK_TOPIC, OVERMAP_TERRAIN)
    val tp = getString(TYPE)(jsObject).toLowerCase

    val isObsolete: Boolean = {
      jsObject \ OBSOLETE match {
        case JsDefined(value) => value.as[Boolean]
        case JsUndefined() => false
      }
    }

    val isBlack: Boolean = {
      blacklist.contains(tp)
    }

    !isObsolete && !isBlack
  }

  private def loadJson(jsObject: JsObject): Unit = {
    val tp = getString(TYPE)(jsObject).toLowerCase
    var pend = jsObject
    if (shouldLoad(jsObject)) {
      pend = addCustomField(tp, pend)

      val key = if (ITEM_TYPES.contains(tp)) ITEM else tp

      def cacheObj(ident: String, pend: JsObject): Unit = {
        handlerCtxt.objCache(key) += ident -> pend
        log.debug(s"json loaded: ${ident -> pend}")
      }

      // 获取该json的唯一标识，不同type的唯一标识生成算法可能不一样
      getIdent(tp)(pend) match {
        case Left(ident) =>
          cacheObj(ident, pend)
          // cache tool substitution
          if (tp == TOOL) {
            val sub = getString(SUB)(pend)
            handlerCtxt.addToolSub(sub, ident)
          }
        case Right(identList) => identList.foreach(cacheObj(_, pend))
      }
    }
  }

  /** add custom field, see [[common.Type]] */
  private def addCustomField(tp: String, jsObject: JsObject): JsObject = {
    var pend = jsObject
    if (ITEM_TYPES.contains(tp)) {
      pend ++= Json.obj(
        CRAFT_TO -> JsArray(),
        CAN_CRAFT -> JsBoolean(false),
        UNCRAFT_FROM -> JsArray(),
        CAN_UNCRAFT -> JsBoolean(false)
      )
      if (tp == BOOK) {
        pend ++= jsObject ++ Json.obj(
          RECIPES -> JsArray()
        )
      }
    }
    pend
  }

  // 将所有加载到的json进行copy-from处理
  private def copyFrom(): Unit = {
    handlerCtxt.objCaches.foreach {
      cacheMap =>
        val (_, objCache) = cacheMap
        objCache.foreach {
          pair =>
            val (ident, obj) = pair
            handleCopyFrom(obj, ident, objCache)
        }
    }
  }

  /**
   * 处理json inheritance
   * 继承规则：https://github.com/CleverRaven/Cataclysm-DDA/blob/master/doc/JSON_INHERITANCE.md
   *
   * @param ident 该类型的唯一标识
   */
  private def handleCopyFrom(jsObject: JsObject, ident: String,
                             objCache: mutable.Map[String, JsObject]): JsObject = {

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
        case JsError(_) => Some(obj)
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

    // 判断该json是否需要继承
    getString(COPY_FROM)(jsObject) match {
      case "" => jsObject
      case parId =>
        objCache.get(parId) match {
          case Some(temp) =>
            val parent = handleCopyFrom(temp, parId, objCache)
            val newObj = parent ++ jsObject - COPY_FROM - ABSTRACT
            val res = for {
              i <- valueInherit(newObj, RELATIVE)
              j <- valueInherit(i, PROPORTIONAL)
              k <- fieldInherit(j, EXTEND)
              l <- fieldInherit(k, DELETE)
            } yield l
            objCache(ident) = res.get // copy-from成功，修改缓存以供后续继承
            log.debug(s"copy-from handle success, json: ${res.get}")
            res.get
          // 资源文件出错
          case None =>
            log.error(s"In id: $ident, parent: $parId required, but not found in files")
            jsObject
        }
    }
  }

  private def postProcess(): Unit = {
    // 任何字段处理都必须在本循环处理完
    handlerCtxt.objCaches.foreach {
      cacheMap =>
        val (tp, objCache) = cacheMap
        tp match {
          case ITEM => ItemHandler.handle(objCache)
          case RECIPE | UNCRAFT => RecipeHandler.handle(objCache)
          case MONSTER => MonsterHandler.handle(objCache)
          case _ => CommonHandler.handle(objCache)
        }
    }

    // 翻译、构建索引等
    handlerCtxt.objCaches.foreach {
      cacheMap =>
        val (tp, objCache) = cacheMap
        tp match {
          case ITEM => ItemHandler.finalize(objCache)
          case RECIPE | UNCRAFT => RecipeHandler.finalize(objCache)
          case MONSTER => MonsterHandler.finalize(objCache)
          case _ => CommonHandler.finalize(objCache)
        }
    }
  }
}