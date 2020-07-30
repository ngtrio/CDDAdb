package handler

import play.api.libs.json.JsObject

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * @param objCaches // type -> Map(ident -> jsObject)
 * @param indexed   // index keys -> jsObject
 */
case class HandlerContext(objCaches: mutable.Map[String, mutable.Map[String, JsObject]] = mutable.Map(),
                          indexed: ListBuffer[(List[String], JsObject)] = ListBuffer()) {

  def objCache(tp: String): mutable.Map[String, JsObject] = {
    objCaches.getOrElseUpdate(tp, mutable.Map())
  }

  def clear(): Unit = {
    objCaches.clear()
    indexed.clear()
  }
}