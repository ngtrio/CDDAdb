package handler

import common.Field.TYPE
import manager.HandlerContext
import play.api.Logger
import play.api.libs.json.JsObject
import utils.JsonUtil.getString

import scala.collection.mutable

object CommonHandler {
  private val log = Logger(CommonHandler.getClass)

  def handle(json: JsObject): JsObject = {
    json
  }
}
