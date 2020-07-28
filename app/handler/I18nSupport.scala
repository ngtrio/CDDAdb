package handler

import common.Field
import manager.TransManager
import play.api.libs.json._
import utils.JsonUtil.{getField, getString}

trait I18nSupport {
  implicit val trans: TransManager.type = TransManager

  protected def tranObj(jsObject: JsObject, toTran: List[String]): JsObject = {
    var res = jsObject
    toTran.foreach {
      field =>
        res = tranField(res, field)
    }
    res
  }

  /**
   * field的翻译处理
   */
  private def tranField(implicit jsObject: JsObject, field: String): JsObject = {
    val tran = getField(field, jsObject, "") {
      case res: JsString =>
        val msgid = res.as[String]
        getTran(msgid, "")
      case res: JsObject =>
        var msgid = getString(Field.STR_SP)(res)
        msgid = getField(Field.STR, res, msgid)(_.as[String])
        // 存在json中没有复数形式，但是翻译中有复数形式的情况，所有键值都采用单数形式吧
        // msgid = getField("str_pl", res, msgid)(_.as[String])
        val ctxt = getString(Field.CTXT)(res)
        getTran(msgid, ctxt)
      case res: JsArray =>
        val msgid = res.value(0).as[String]
        getTran(msgid, "")
      // case res: _ =>
      // log.warn(s"name format not supported, format: $res")
      // FIXME: trans to what?
    }

    val tf = __.json.update((__ \ field).json.put(JsString(tran)))
    jsObject.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(_) => jsObject
    }
  }

  def getTran(msg: String, ctxt: String): String = trans.get(msg, ctxt)
}
