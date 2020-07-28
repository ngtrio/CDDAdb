package handler

import common.Field._
import common.Type._
import play.api.libs.json.JsObject
import utils.JsonUtil._

object ItemHandler extends Handler[Item]
  with CopyFromSupport with I18nSupport {
  override var prefix = s"$ITEM."

  override def handle(obj: JsObject): Option[(List[String], JsObject)] = {
    val tp = getString(TYPE)(obj)
    val ident = getString(ID)(obj)
    handleCopyFrom(obj, ident) match {
      case Some(value) =>
        var pend = tranObj(value, List(NAME, DESCRIPTION))
        if (!hasField(ABSTRACT)(pend)) {
          val name = getString(NAME)(pend).toLowerCase
          idxKeys += s"$prefix$tp.$name"
        }
        Some(idxKeys.toList -> pend)
      case None => None
    }
  }
}
