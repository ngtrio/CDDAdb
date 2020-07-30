package handler

import common.Type._
import play.api.Logger
import play.api.libs.json.JsObject

import scala.collection.mutable

object RecipeHandler extends Handler {
  private val log = Logger(RecipeHandler.getClass)
  private var prefix: String = RECIPE

  override def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    log.info(s"handling ${objs.size} objects, wait...")
    objs.foreach {
      pair =>
        val (ident, obj) = pair
    }
    ???
  }
}
