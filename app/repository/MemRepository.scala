package repository

import common.Field
import javax.inject.{Inject, Singleton}
import manager.ResourceManager
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@Singleton
class MemRepository @Inject()(manager: ResourceManager) extends Repository {
  private val log = Logger(this.getClass)

  // key -> JsValue
  private[this] val idx = mutable.Map[String, JsValue]()

  // start up
  updateIndexes()

  override protected def updateIndexes(): Unit = {
    manager.update().foreach {
      res =>
        res.foreach {
          pair =>
            val (key, value) = pair
            idx += key -> value
        }
    }
  }

  override def getOne(key: String): JsValue = {
    log.info(s"get: $key")
    idx.get(key) match {
      case Some(value) => value
      case None => Json.obj()
    }
  }

  override def listNameInfo(tp: String): JsArray = {
    import utils.JsonUtil._
    log.info(s"list: $tp")

    val res = ListBuffer[JsObject]()
    idx.foreach {
      pair =>
        val k = pair._1
        val v = pair._2
        if (k.startsWith(s"$tp:")) {
          val id = k.replace(s"$tp:", "")
          val name = getString(Field.NAME)(v)
          val symbol = getString(Field.SYMBOL)(v)
          val color = getField(Field.COLOR, v, JsArray())(_.as[JsArray])

          res += Json.obj(
            Field.ID -> id,
            Field.NAME -> name,
            Field.SYMBOL -> symbol,
            Field.COLOR -> color
          )
        }
    }
    JsArray(res)
  }
}
