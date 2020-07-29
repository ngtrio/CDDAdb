package handler

import common.Field._
import common.Type._
import play.api.libs.json.JsObject
import utils.JsonUtil._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ItemHandler extends Handler
  with CopyFromSupport with I18nSupport with ColorSymbolSupport {
  override protected var prefix = s"$ITEM."
  override protected var objCache: mutable.Map[String, JsObject] = mutable.Map()
  override var cpfCache: ListBuffer[JsObject] = ListBuffer()

  override def handle(obj: JsObject): Option[(List[String], JsObject)] = {
    val tp = getString(TYPE)(obj).toLowerCase
    if (ITEM_TYPES.contains(tp)) {
      val ident = getString(ID)(obj)
      handleCopyFrom(obj, ident).map {
        value =>
          implicit var pend: JsObject = tranObj(value, NAME, DESCRIPTION)
          pend = handleColor

          val idxKeys = ListBuffer[String]()
          if (!hasField(ABSTRACT)) {
            val name = getString(NAME)
            idxKeys += s"$prefix$tp.$name"
          }
          idxKeys.toList -> pend
      }
    } else None
  }
}
