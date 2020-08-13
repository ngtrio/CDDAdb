package manager

import play.api.libs.json.JsValue

trait ResourceManager {
  def update(): Option[List[(String, JsValue)]]
}
