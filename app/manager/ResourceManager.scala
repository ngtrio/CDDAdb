package manager

import com.google.inject.ImplementedBy
import handler.Handler
import play.api.libs.json.JsObject

@ImplementedBy(classOf[MemResourceManager])
trait ResourceManager extends Manager {
  // 根据索引取值
  def index(key: String): JsObject

  def list(tp: String): List[JsObject]

  def withHandler(handler: Handler*): this.type

  def update(): Unit
}
