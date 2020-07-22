package cddadb.manager

import org.scalatest.wordspec.AnyWordSpecLike

class MemResourceManagerTest extends AnyWordSpecLike {
  "_" in {
    val man = new MemResourceManager(dataPath =
      List("data/cdda/data/json/items", "data/cdda/data/core"))
    man.update()
  }
}
