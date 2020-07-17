package cddadb.utils

import java.io._

import scala.collection.mutable.ListBuffer
import scala.io.Source

object FileUtil {
  def classpathFileReader(filename: String): Reader =
    Source.fromResource(filename).reader()

  def workDirFileReader(filename: String): Reader =
    new BufferedReader(new FileReader(filename))

  // 移动文件
  def mv(dir: String): Unit = ???

  // 删除文件，r -> recursive
  def rm(path: String, r: Boolean): Nothing = ???

  // 列出路径path下的所有文件和文件夹
  def ls(path: File): List[File] = {
    var res = new ListBuffer[File]
    if (!path.exists) {
      throw new FileNotFoundException
    }
    path.listFiles.foreach(res += _)
    res.toList
  }

  case class FileNode(file: File, subNodes: ListBuffer[FileNode]) {
    override def toString: String = {
      draw(this, hasPar = false, "", parHasBro = false, hasBro = false)
    }

    // ├ └ ─ │
    def draw(node: FileNode, hasPar: Boolean, parPrefix: String,
             parHasBro: Boolean, hasBro: Boolean): String = {
      var prefix = ""
      var res = ""
      if (hasPar) {
        prefix += (if (hasPar) parPrefix else "")
        prefix += (if (parHasBro) "│ " else "  ")
        res = prefix
        res += (if (hasBro) "├─" else "└─")
      }
      res += s"${node.file.getName}\n"

      var num = node.subNodes.size
      for (n <- node.subNodes) {
        res += draw(n, hasPar = true, prefix, parHasBro = hasBro, num > 1)
        num -= 1
      }
      res
    }
  }

  // 构建路径root下所有文件，文件夹的树形结构
  def tree(root: File): FileNode = {
    doTree(root)
  }

  private def doTree(root: File): FileNode = {
    val node = FileNode(root, ListBuffer())
    if (!root.isDirectory) {
      node
    } else {
      ls(root).foreach {
        child => node.subNodes += doTree(child)
      }
      node
    }
  }
}
