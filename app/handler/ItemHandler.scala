package handler

import common.Field._
import common.Type
import play.api.Logger
import play.api.libs.json.JsObject
import utils.I18nUtil.tranObj
import utils.JsonUtil._

import scala.collection.mutable

object ItemHandler extends Handler with ColorSymbolSupport {
  private val log = Logger(ItemHandler.getClass)
  private val prefix = Type.ITEM

  override def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    log.debug(s"handling ${objs.size} objects, wait...")

    objs.foreach {
      pair =>
        val (ident, obj) = pair
        implicit var pend: JsObject = obj
        pend = handleColor

        objs(ident) = pend
    }
  }

  override def finalize(objs: mutable.Map[String, JsObject])
                       (implicit ctxt: HandlerContext): Unit = {
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        val pend = tranObj(obj, NAME, DESCRIPTION)
        //        val name = getString(NAME)(pend)
        val tp = getString(TYPE)(pend)
        ctxt.addIndex(
          s"$prefix:$tp:$ident" -> pend,
          //          s"$prefix:$tp:$name" -> JsString(s"$prefix:$tp:$ident")
        )
    }
  }
}
