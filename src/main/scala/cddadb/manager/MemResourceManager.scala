package cddadb.manager

import play.api.libs.json.JsObject

class MemResourceManager(poPath: String = "data/zh.po",
                         dataPath: String = "data/cdda/data/json")
  extends BaseResourceManager(poPath, dataPath) {

  // 一个名字可以有多个对象
  // type -> (name -> List[JsObject])
  private[this] val index = Map[String, Map[String, List[JsObject]]]()

  override def update(): Unit = {
    updateTrans()
    loadDataFiles()
  }

  override protected def postProcess(key: String, jo: JsObject): Unit = {
    println(s"indexed: $key, json: $jo")
  }
}
