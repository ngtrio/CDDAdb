package handler

import play.api.libs.json.JsObject


trait Handler {
  protected var prefix: String

  // 处理成功将返回一个待注册索引的key列表，和处理完成的json obj
  // 类型不匹配，或者json继承失败（如果需要）将返回None
  def handle(obj: JsObject): Option[(List[String], JsObject)]
}

object Handler {
  val handlers = List(ItemHandler, MonsterHandler)
}