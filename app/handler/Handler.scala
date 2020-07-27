package handler

import common.Type
import manager.TransManager
import play.api.libs.json.JsObject


trait Handler[T] {
  def handle(obj: JsObject): JsObject

  implicit val trans: TransManager.type = TransManager
}

trait DefaultHandler {
  implicit val monsterHandler: Handler[Type.Monster] = MonsterHandler
}

object Handler extends DefaultHandler {

}