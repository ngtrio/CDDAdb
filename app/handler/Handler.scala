package handler

import manager.{HandlerContext, Mod}
import play.api.libs.json.JsObject

import scala.collection.mutable


trait Handler {
  def handle(json: JsObject): JsObject

  def finalize(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit

  protected def genKey(prefix: String, value: String): String = s"$prefix:$value"
}

object Handler {
  val handlers = List(MonsterHandler)
}
