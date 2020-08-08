package handler

import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * @param objCaches type -> Map(ident -> jsObject)
 * @param indexed   index keys -> jsObject
 * @param toolSub   toolId -> List(substitutions to toolId)
 */
class HandlerContext(val objCaches: mutable.Map[String, mutable.Map[String, JsObject]] = mutable.Map(),
                     val indexed: mutable.Map[String, JsValue] = mutable.Map(),
                     val toolSub: mutable.Map[String, ListBuffer[String]] = mutable.Map()) {

  private val log = Logger(this.getClass)

  def objCache(tp: String): mutable.Map[String, JsObject] = {
    objCaches.getOrElseUpdate(tp, mutable.Map())
  }

  def clear(): Unit = {
    objCaches.clear()
    indexed.clear()
  }

  def addIndex(indexes: (String, JsValue)*): Unit = {
    indexes.foreach {
      pair =>
        log.info(s"indexed: ${pair._1}")
        indexed += pair
    }
  }

  def addToolSub(toSub: String, sub: String): Unit = {
    val subs = toolSub.getOrElseUpdate(toSub, ListBuffer[String]())
    subs += sub
  }

  def getToolSub(toSub: String): List[String] = {
    toolSub.getOrElse(toSub, ListBuffer[String]()).toList
  }

  def getIndex(key: String): Option[JsValue] = indexed.get(key)

  def updateIndex(key: String, value: JsValue): Unit = {
    indexed(key) = value
  }
}