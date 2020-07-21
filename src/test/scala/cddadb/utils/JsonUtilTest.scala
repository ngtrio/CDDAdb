package cddadb.utils

import org.scalatest.wordspec.AnyWordSpecLike

class JsonUtilTest extends AnyWordSpecLike {
  "_" in {
    import FileUtil._
    import play.api.libs.json._
    JsonUtil.fromFile(workDirFile("data/object.json")).foreach {
      obj =>
        val tf = (__).json.update((__ \ "a").read[JsObject])
        obj.transform(tf) match {
          case JsSuccess(value, _) =>
            println(value)
          case JsError(errors) =>
            println(errors)
        }
    }
  }
}
