package handler

import common.Field.TYPE
import manager.HandlerContext
import play.api.Logger
import play.api.libs.json.JsObject
import utils.JsonUtil.getString

import scala.collection.mutable

object CommonHandler extends Handler {
  private val log = Logger(CommonHandler.getClass)

  override def handle(json: JsObject): JsObject = {
    json
  }

  override def finalize(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        val tp = getString(TYPE)(obj).toLowerCase
        ctxt.addIndex(s"$tp:$ident" -> obj)
    }
  }
}
