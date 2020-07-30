package manager

import com.google.inject.ImplementedBy
import play.api.libs.json.JsObject

@ImplementedBy(classOf[BaseResourceManager])
trait ResourceManager extends Manager {
  def update(): List[(List[String], JsObject)]
}
