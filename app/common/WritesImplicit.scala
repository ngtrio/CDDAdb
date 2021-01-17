package common

import play.api.libs.json.{JsArray, JsValue, Json, Writes}

/**
 * @author jaron
 *         created on 2021/1/16 at 下午2:42
 */
object WritesImplicit {
  implicit val resultListWrites: Writes[Result] = new Writes[Result] {
    override def writes(result: Result): JsValue =
      Json.obj(
        "modId" -> result.modId,
        "json" -> result.json
      )
  }
}
