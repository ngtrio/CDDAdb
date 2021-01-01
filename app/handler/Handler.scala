package handler

import manager.HandlerContext
import play.api.libs.json.JsObject

import scala.collection.mutable


trait Handler {
  def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit

  def finalize(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit

  protected def genKey(prefix: String, value: String): String = s"$prefix:$value"
}

object Handler {
  val handlers = List(ItemHandler, MonsterHandler)
}
