package cddadb.utils

import java.io.File

import org.scalatest.wordspec.AnyWordSpecLike

class FileUtilTest extends AnyWordSpecLike {
  "_" in {
    import FileUtil._
    print(tree(new File("data/cdda/data/json")))
  }
}
