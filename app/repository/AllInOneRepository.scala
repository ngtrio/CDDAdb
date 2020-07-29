package repository

import common.Field
import handler.Handler
import javax.inject.{Inject, Named, Singleton}
import manager.ResourceManager
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.collection.mutable.ListBuffer

@Singleton
class AllInOneRepository @Inject()(@Named("memrm") manager: ResourceManager) extends Repository {
  manager
    .withHandler(Handler.handlers: _*)
    .update()

  override def getOne(tp: String, name: String): JsObject = {
    manager.index(indexKey(tp, name))
  }

  override def listNameInfo(tp: String): JsArray = {
    import utils.JsonUtil._

    val objs = manager.list(tp)
    val res = ListBuffer[JsObject]()

    objs.foreach {
      implicit obj =>
        val name = getString(Field.NAME)
        val symbol = getString(Field.SYMBOL)
        val color = getField(Field.COLOR, obj, JsArray())(_.as[JsArray])

        res += Json.obj(
          Field.NAME -> name,
          Field.SYMBOL -> symbol,
          Field.COLOR -> color
        )
    }
    JsArray(res)
  }
}
