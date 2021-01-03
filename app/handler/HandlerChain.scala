package handler

import manager.Mod
import play.api.libs.json.JsObject
import utils.JsonUtil

import scala.collection.mutable.ListBuffer

/**
 * @author jaron
 *         created on 2021/1/3 at 上午12:00
 */
class HandlerChain {
  private val _chain = ListBuffer[Handler]()

  def addLast(handler: Handler): HandlerChain = {
    _chain += handler
    this
  }

  def handle(json: JsObject): JsObject = {
    _chain.foldLeft(json) {
      (_, handler) =>
        handler.handle(json)
    }
  }
}

object HandlerChain {
  private val _chain = new HandlerChain().addLast(MonsterHandler)

  def apply(): HandlerChain = _chain
}
