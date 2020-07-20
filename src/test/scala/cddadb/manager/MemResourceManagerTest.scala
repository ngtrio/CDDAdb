package cddadb.manager

import org.scalatest.wordspec.AnyWordSpecLike

class MemResourceManagerTest extends AnyWordSpecLike {
  "_" in {
    val man = new MemResourceManager()
    man.update()
  }
}
