package cddadb.utils

import org.scalatest.wordspec.AnyWordSpecLike

class JsonUtilTest extends AnyWordSpecLike {
  "_" in {
    import FileUtil._
    println {
      JsonUtil.fromFile(workDirFile("data/object.json"))
    }
    println {
      JsonUtil.fromFile(workDirFile("data/array.json"))
    }
  }
}
