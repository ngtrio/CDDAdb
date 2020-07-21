package cddadb.manager

import org.scalatest.wordspec.AnyWordSpecLike

class MemResourceManagerTest extends AnyWordSpecLike {
  "_" in {
    val man = new MemResourceManager(dataPath = "data/cdda/data/json/items/ammo")
    man.update()
  }
}
