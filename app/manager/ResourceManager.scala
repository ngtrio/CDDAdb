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
class ResourceManager {
  private val log: Logger = Logger(this.getClass)

  private var poPath: String = _
  private var jsonPath: List[String] = _
  private var notUpdate: Boolean = _
  implicit private val handlerCtx: HandlerContext = new HandlerContext()

  @Inject
  def this(config: Configuration) = {
    this()
    this.poPath = config.get[String]("poPath")
    this.jsonPath = config.get[Seq[String]]("jsonPath").toList
    this.notUpdate = config.get[Boolean]("notUpdate")
  }


  private def clear(): Unit = {
    handlerCtx.clear()
  }

  private def loadJson(jsObject: JsObject): Unit = {
    val tp = getString(TYPE) (jsObject).toLowerCase
    var pend = jsObject
//    if (shouldLoad(jsObject)) {
      pend = addCustomField(tp, pend)

      val key = if (ITEM_TYPES.contains(tp)) ITEM else tp

      def cacheObj(ident: String, pend: JsObject): Unit = {
        handlerCtx.objCache(key) += ident -> pend
        log.debug(s"json loaded: ${ident -> pend}")
      }

      // 获取该json的唯一标识，不同type的唯一标识生成算法可能不一样
      getIdent(tp)(pend).foreach {
        ident =>
          cacheObj(ident, pend)
          // cache tool substitution
          if (tp == TOOL) {
            val sub = getString(SUB)(pend)
            handlerCtx.addToolSub(sub, ident)
          }
      }
//    }
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
}
