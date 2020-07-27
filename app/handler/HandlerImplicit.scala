package handler

import play.api.libs.json.JsObject

object HandlerImplicit {

  implicit class ObjWrapper(obj: JsObject) {
    def process[T](implicit handler: Handler[T]): JsObject = {
      handler.handle(obj)
    }
  }

}