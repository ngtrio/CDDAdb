package repository

import play.api.libs.json.{JsArray, JsValue}

trait Repository {
  def getOne(key: String): JsValue

  def listNameInfo(tp: String): JsArray

  protected def indexKey(tp: String, name: String): String = s"$tp.$name"

  protected def updateIndexes(): Unit
}
