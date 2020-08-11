package utils

import java.io.{File, FileInputStream}

import org.scalatest.wordspec.AnyWordSpecLike
import utils.FileUtil._

class FileUtilTest extends AnyWordSpecLike {
  "unzip file" in {
    unzip("data/cdda.zip", "data/cdda")
  }

  "write to file" in {
    val file = new File("data/cdda1.zip")
    val newFile = new File("data/cdda2.zip")
    writeToFile(new FileInputStream(file), "data/cdda2.zip", 1)
    println(file.length(), newFile.length())
  }
}
