package manager

import org.scalatest.wordspec.AnyWordSpecLike

class MemResourceManagerTest extends AnyWordSpecLike {
  "_" in {
    val manager = new MemResourceManager(
      poPath = "data/zh.po",
      dataPath = List(
        "data/cdda/data/json",
        "data/cdda/data/core"))
    manager.update()
  }
}
