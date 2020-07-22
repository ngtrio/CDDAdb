package cddadb.utils

import java.io.File

import org.scalatest.wordspec.AnyWordSpecLike

class FileUtilTest extends AnyWordSpecLike {
  "tree should work fine" in {
    import FileUtil._
    print(tree(new File("data/cdda/data/json/items")))
  }

  "ls should work fine" in {
    import FileUtil._
    println(ls(new File("data/cdda/data/json/items"), recursive = true, ONLY_FILE))

  }
}
