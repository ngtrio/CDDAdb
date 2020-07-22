package cddadb.manager

import play.api.libs.json.JsObject

class MemResourceManager(poPath: String = "data/zh.po",
                         dataPath: List[String] = List("data/cdda/data/json"))
  extends BaseResourceManager(poPath, dataPath) {

  // key -> JsObject
  private[this] val idx = Map[String, JsObject]()

  override def update(): Unit = {
    updateTrans()
    loadDataFiles()
  }

  override protected def postProcess(key: String, obj: JsObject): Unit = {
    idx + key -> obj
  }

  override def index(key: String): JsObject = idx(key)
}
