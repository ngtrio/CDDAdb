package utils

import java.io._

import scala.collection.mutable.ListBuffer
import scala.io.Source

object FileUtil {
  def classpathFileReader(filename: String): Reader =
    Source.fromResource(filename).reader()

  def workDirFile(filename: String): File = new File(filename)

  // 移动文件
  def mv(dir: String): Unit = ???

  // 删除文件，r -> recursive
  def rm(path: String, r: Boolean): Nothing = ???

  // modes of ls
  val MIX = 0
  val ONLY_FILE = 1
  val ONLY_DIR = 2

  // 列出文件
  def ls(path: File, recursive: Boolean, mode: Int): List[File] = {
    var res = new ListBuffer[File]
    if (!path.exists) {
      throw new FileNotFoundException
    }
    if (!path.isFile) {
      path.listFiles.foreach {
        f =>
          if (mode match {
            case ONLY_FILE => f.isFile
            case ONLY_DIR => f.isDirectory
            case MIX => true
          }) {
            res += f
          }
          if (recursive) {
            res ++= ls(f, recursive = true, mode)
          }
      }
    }
    res.toList
  }

  case class FileNode(file: File, subNodes: ListBuffer[FileNode]) {
    override def toString: String = {
      draw(this, hasPar = false, "", parHasBro = false, hasBro = false)
    }

    // 制表符 ├ └ ─ │
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
      ls(root, recursive = false, MIX).foreach {
        child => node.subNodes += doTree(child)
      }
      node
    }
  }
}
