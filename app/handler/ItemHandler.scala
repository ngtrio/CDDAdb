package handler

import common.Field._
import play.api.Logger
import play.api.libs.json.JsObject
import utils.JsonUtil._

import scala.collection.mutable

object ItemHandler extends Handler with I18nSupport with ColorSymbolSupport {
  private val log = Logger(ItemHandler.getClass)

  override def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    log.info(s"handling ${objs.size} objects, wait...")

    objs.foreach {
      pair =>
        val (ident, obj) = pair
        implicit var pend: JsObject = tranObj(obj, NAME, DESCRIPTION)
        pend = handleColor

        val name = getString(NAME)
        val tp = getString(TYPE)
        val idxKeys = genKey(tp, name) :: genKey(tp, ident) :: Nil

        log.info(s"registered keys: $idxKeys")
        ctxt.indexed += idxKeys -> pend
    }
  }
}
