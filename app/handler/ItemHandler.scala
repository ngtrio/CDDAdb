package handler

import common.Field._
import play.api.Logger
import play.api.libs.json.JsObject
import utils.I18nUtil.tranObj
import utils.JsonUtil._

import scala.collection.mutable

object ItemHandler extends Handler with ColorSymbolSupport {
  private val log = Logger(ItemHandler.getClass)

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
        val pend = tranObj(obj, NAME, DESCRIPTION, CRAFT_TO, RECIPES)
        val tp = getString(TYPE)(pend).toLowerCase
        ctxt.addIndex(
          s"$tp:$ident" -> pend,
        )
    }
  }
}
