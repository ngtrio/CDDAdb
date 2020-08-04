package manager

import com.google.inject.ImplementedBy
import play.api.libs.json.JsValue

@ImplementedBy(classOf[BaseResourceManager])
trait ResourceManager {
  def update(): List[(String, JsValue)]
}
