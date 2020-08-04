package handler

import common.Field
import play.api.libs.json._
import utils.JsonUtil.transform
import utils.StringUtil.parseColor

trait ColorSymbolSupport {
  protected def handleColor(implicit obj: JsObject): JsObject = {
    val tf = (__ \ Field.COLOR).json.update(__.read[JsString].map(
      str => JsArray(parseColor(str.as[String]).map(JsString))))
    transform(tf, obj)
  }
}
