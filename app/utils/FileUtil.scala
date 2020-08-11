package utils

import java.io.{File, _}
import java.nio.charset.Charset

import common.FileType
import play.api.Logger

import scala.collection.mutable.ListBuffer
import scala.io.Source

object FileUtil {
  private val log = Logger(FileUtil.getClass)
  private val File = better.files.File

  def classpathFileReader(filename: String): Reader =
    Source.fromResource(filename).reader()

  def workDirFile(filename: String): File = new File(filename)

  // 移动文件
  def mv(from: String, to: String): Unit = File(from).moveTo(File(to))

  // 删除文件，recursive
  def rm(path: String): Unit = File(path).delete()

  // unzip
  def unzip(file: String, to: String): Unit = {
    TimeUtil.stopwatch {
      log.info(s"unzip: $file to $to")
      val toDir = File(to)
      if (toDir.exists) {
        rm(to)
      }
      File(file).unzipTo(File(to))
    }
  }

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

  def writeToFile(is: InputStream, path: String, fileType: Int): Unit = {
    val file = new File(path)
    if (fileType == FileType.TEXT) {
      val reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))
      reader.transferTo(new FileWriter(file))
    } else {
      val bis = new BufferedInputStream(is)
      bis.transferTo(new FileOutputStream(file))
    }
  }
}
