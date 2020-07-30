package repository

import common.Field
import javax.inject.{Inject, Named, Singleton}
import manager.ResourceManager
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Singleton
class MemRepository @Inject()(@Named("memrm") manager: ResourceManager) extends Repository {
  private val log = Logger(this.getClass)

  // key -> JsObject
  private[this] val idx = mutable.Map[String, JsObject]()

  // start up
  updateIndexes()

  override protected def updateIndexes(): Unit = {
    manager.update().foreach {
      pair =>
        val (keys, obj) = pair
        keys.foreach {
          key =>
            idx += key -> obj
        }
    }
  }

  override def getOne(key: String): JsObject = {
    log.info(s"get: $key")
    idx.get(key) match {
      case Some(value) => value
      case None => Json.obj()
    }
  }

  override def listNameInfo(tp: String): JsArray = {
    import utils.JsonUtil._

    val res = ListBuffer[JsObject]()
    idx.foreach {
      pair =>
        val k = pair._1
        val v = pair._2
        if (k.contains(tp)) {
          val name = getString(Field.NAME)(v)
          val symbol = getString(Field.SYMBOL)(v)
          val color = getField(Field.COLOR, v, JsArray())(_.as[JsArray])

          res += Json.obj(
            Field.NAME -> name,
            Field.SYMBOL -> symbol,
            Field.COLOR -> color
          )
        }
    }
    JsArray(res)
  }
}
