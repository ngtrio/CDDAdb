package handler

import common.Type
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json

class ObjWrapperTest extends AnyWordSpecLike {
  "_" in {
    val obj = Json.obj {
      "a" -> 1
    }

    import handler.HandlerImplicit._
    val str = obj.process[Type.Monster]

  }
}
