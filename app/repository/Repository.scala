package repository

import play.api.libs.json.{JsArray, JsObject}

trait Repository {
  def getOne(tp: String, name: String): JsObject

  def listNameInfo(tp: String): JsArray

  protected def indexKey(tp: String, name: String): String = s"$tp.$name"
}
