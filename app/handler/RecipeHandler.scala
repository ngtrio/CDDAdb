package handler

import common.Field._
import common.Type._
import play.api.libs.json.JsObject
import utils.JsonUtil.getString

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RecipeHandler extends Handler with CopyFromSupport {
  override protected var prefix: String = s"$RECIPE."
  override protected var objCache: mutable.Map[String, JsObject] = mutable.Map()
  override var cpfCache: ListBuffer[JsObject] = ListBuffer()

  override def handle(obj: JsObject): Option[(List[String], JsObject)] = {
    val ident = getString(RESULT)(obj) + getString(ID_SUFFIX)(obj)
    ???
  }
}
