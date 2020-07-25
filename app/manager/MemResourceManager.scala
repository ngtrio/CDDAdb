package manager

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}

import scala.collection.mutable.ListBuffer

//object MemResourceManager {
//  def apply(poPath: String = "data/zh.po",
//            dataPath: List[String] = List("data/cdda/data/json")): MemResourceManager = {
//    new MemResourceManager(poPath, dataPath)
//  }
//}

@Singleton
class MemResourceManager extends BaseResourceManager {

  @Inject
  def this(config: Configuration) {
    this()
    this.poPath = config.get[String]("poPath")
    this.dataPath = config.get[Seq[String]]("dataPath").toList
  }

  // for direct usage
  def this(poPath: String, dataPath: List[String]) {
    this()
    this.poPath = poPath
    this.dataPath = dataPath
  }

  // key -> JsObject
  private[this] val idx = Map[String, JsObject]()

  override def update(): Unit = {
    updateTrans()
    loadDataFiles()
  }

  override protected def postProcess(key: String, obj: JsObject): Unit = {
    idx += key -> obj
    log.info(s"registered: $key")
  }

  override def getByTypeName(tp: String, name: String): JsObject = {
    val key = indexKey(tp, name)
    log.info(s"get: $key")
    idx.get(key) match {
      case Some(value) => value
      case None => Json.obj()
    }
  }

  override def listByType(tp: String): List[JsObject] = {
    val res = ListBuffer[JsObject]()
    idx.foreach {
      pair =>
        val k = pair._1
        val v = pair._2
        if (k.contains(tp)) {
          res += v
        }
    }
    res.toList
  }
}
