package manager

import handler.Handler
import org.scalatest.wordspec.AnyWordSpecLike

class MemResourceManagerTest extends AnyWordSpecLike {
  "_" in {
    val manager = new MemResourceManager(
      poPath = "data/zh.po",
      dataPath = List(
        //        "data/cdda/data/json/items",
        "data/cdda/data/json/monsters",
        "data/cdda/data/core"))

    manager
      .withHandler(Handler.handlers: _*)
      .update()
  }
}
