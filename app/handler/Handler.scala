package handler

import manager.{HandlerContext, Mod}
import play.api.libs.json.JsObject

import scala.collection.mutable


trait Handler {
  def handle(json: JsObject): JsObject

  protected def canHandle(json: JsObject): Boolean

  protected def genKey(prefix: String, value: String): String = s"$prefix:$value"
}

object Handler {
  val handlers = List(MonsterHandler)
}
