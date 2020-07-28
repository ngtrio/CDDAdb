package handler

import common.Type
import play.api.libs.json.JsObject

import scala.collection.mutable.ListBuffer


trait Handler[T] {
  protected var prefix: String
  protected val idxKeys: ListBuffer[String] = ListBuffer[String]()

  // 处理成功将返回一个待注册索引的key列表，和处理完成的json obj
  def handle(obj: JsObject): Option[(List[String], JsObject)]
}


trait DefaultHandler {
  implicit val monsterHandler: Handler[Type.Monster] = MonsterHandler
  implicit val itemHandler: Handler[Type.Item] = ItemHandler
}

object Handler extends DefaultHandler {

}