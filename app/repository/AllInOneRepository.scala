package repository

import common.Field
import javax.inject.{Inject, Named, Singleton}
import manager.ResourceManager
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.collection.mutable.ListBuffer

@Singleton
class AllInOneRepository @Inject()(@Named("memrm") manager: ResourceManager) extends Repository {

  manager.update()

  override def getOne(tp: String, name: String): JsObject = {
    manager.getByTypeName(tp, name)
  }

  override def listNameInfo(tp: String): JsArray = {
    import utils.JsonUtil._

    val objs = manager.listByType(tp)
    val res = ListBuffer[JsObject]()

    objs.foreach {
      obj =>
        val `type` = getStringField(Field.TYPE, obj).toLowerCase
        val name = getStringField(Field.NAME, obj)
        val symbol = getStringField(Field.SYMBOL, obj)
        val color = getField(Field.COLOR, obj, JsArray())(_.as[JsArray])

        if (`type` == tp) {
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
