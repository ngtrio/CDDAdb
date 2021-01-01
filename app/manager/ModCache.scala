package manager

import common.Field
import play.api.Logger
import utils.FileUtil
import utils.FileUtil.{ONLY_FILE, ls}
import utils.JsonUtil.{fromFile, getArray, getString}

import java.io.File
import java.nio.file.Path
import scala.collection.mutable

/**
 * json cache, mod id -> Mod
 *
 * @author jaron
 *         created on 2020/12/27 at 下午9:43
 */
class ModCache {

  private val mods = mutable.Map[String, Mod]()
  private val modMetas = mutable.Map[String, ModMeta]()

  def load(modsPath: String): Unit = {
    val modDirs = FileUtil.ls(new File(modsPath), recursive = false, FileUtil.ONLY_DIR)
    modDirs.foreach(loadModMeta)
    modMetas.foreach(pair => loadMod(pair._2))
    handleCopyFrom()
  }

  private def loadModMeta(modDir: File): Unit = {
    val modinfo = s"${modDir.getAbsolutePath}/modinfo.json"
    val infoJson = fromFile(new File(modinfo)).head
    val modId = getString(Field.ID)(infoJson)
    val modName = getString(Field.NAME)(infoJson)
    val modDesc = getString(Field.DESCRIPTION)(infoJson)
    val modDependencies = getArray(Field.DEPENDENCIES)(infoJson).value.map(_.as[String]).toList
    val path = getString(Field.PATH)(infoJson)
    // e.g. mod dda
    val modSrcDir = if (path != "") {
      Path.of(modDir.getAbsolutePath, path).toFile
    } else {
      modDir
    }

    modMetas += modId -> ModMeta(modId, modName, modDesc, modDependencies, modSrcDir)
  }

  private def loadMod(meta: ModMeta): Unit = {
    if (!mods.contains(meta.id)) {
      val dependencies = meta.dependencies
      val modDir = meta.modDir

      dependencies.foreach {
        depId =>
          loadMod(modMetas(depId))
      }

      val mod = new Mod(meta)
      val files = ls(modDir, recursive = true, ONLY_FILE)
      files.foreach(fromFile(_).foreach(mod.loadJson(_)))
      mods += meta.id -> mod
    }
  }

  private def handleCopyFrom(): Unit = {
    mods.keys.foreach {
      modId =>
        val meta = modMetas(modId)
        val depMod = meta.dependencies.map(id => mods(id))
        mods(modId).handleCopyFrom(depMod)
    }
  }
}
