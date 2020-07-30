package handler

import play.api.libs.json.JsObject

import scala.collection.mutable


trait Handler {
  // 处理成功将返回一个待注册索引的key列表，和处理完成的json obj
  // 类型不匹配，或者json继承失败（如果需要）将返回None
  def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit

  protected def genKey(prefix: String, value: String): String = s"$prefix.$value"
}

object Handler {
  val handlers = List(ItemHandler, MonsterHandler)
}