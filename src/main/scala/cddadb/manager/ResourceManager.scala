package cddadb.manager

import play.api.libs.json.JsObject

trait ResourceManager {
  def update(): Unit

  def index(key: String): JsObject
}
