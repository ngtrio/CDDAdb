package handler

import common.Field._
import common.Type._
import play.api.libs.json.JsObject
import utils.JsonUtil.getString

object RecipeHandler extends Handler[Recipe] with CopyFromSupport {
  override protected var prefix: String = s"$RECIPE."

  override def handle(obj: JsObject): Option[(List[String], JsObject)] = {
    val ident = getString(RESULT)(obj) + getString(ID_SUFFIX)(obj)
    ???
  }
}
