package handler

import play.api.libs.json.JsObject

object HandlerImplicit {

  implicit class ObjWrapper(obj: JsObject) {
    def process[T](implicit handler: Handler[T]): Option[(List[String], JsObject)] = {
      handler.handle(obj)
    }
  }
}