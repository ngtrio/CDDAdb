package handler

import common.Field
import play.api.libs.json._
import utils.StringUtil.parseColor

trait ColorSymbolSupport {
  protected def handleColor(implicit obj: JsObject): JsObject = {
    val tf = (__ \ Field.COLOR).json.update(__.read[JsString].map(str => JsArray(parseColor(str.as[String]).map(JsString))))
    obj.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(_) => obj
    }
  }
}
