package manager

import com.google.inject.ImplementedBy
import play.api.libs.json.JsObject

@ImplementedBy(classOf[MemResourceManager])
trait ResourceManager {
  def update(): Unit

  def getByTypeName(tp: String, name: String): JsObject

  def listByType(tp: String): List[JsObject]
}
