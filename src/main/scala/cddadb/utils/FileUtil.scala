package cddadb.utils

import java.io.Reader

import scala.io.Source

object FileUtil {
  def classpathFileReader(filename: String): Reader =
    Source.fromResource(filename).reader()
}
