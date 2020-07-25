package repository

import play.api.libs.json.{JsArray, JsObject}

trait Repository {
  def getOne(tp: String, name: String): JsObject

  def listNameInfo(tp: String): JsArray
}
