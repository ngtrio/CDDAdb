package manager

import common.Field
import play.api.{Configuration, Logger}
import play.api.libs.json.JsObject
import utils.FileUtil
import utils.FileUtil.{ONLY_FILE, ls}
import utils.JsonUtil.{fromFile, getArray, getString}

import java.io.File
import java.nio.file.Path
import javax.inject.{Inject, Singleton}
import scala.collection.mutable

/**
 * json cache, mod id -> Mod
 *
 * @author jaron
 *         created on 2020/12/27 at 下午9:43
 */
class ModCache @Inject()(val config: Configuration) {

  private val _mods: mutable.Map[String, Mod] = mutable.Map[String, Mod]()
  private val _modMetas = mutable.Map[String, ModMeta]()

  private val notUpdate = config.get[Boolean]("notUpdate")
  private val modsPath = config.get[String]("modsPath")

  if (notUpdate || ResourceUpdater.update()) {
    load(modsPath)
  }

  def mods: mutable.Map[String, Mod] = _mods

  private def load(modsPath: String): Unit = {
    val modDirs = FileUtil.ls(new File(modsPath), recursive = false, FileUtil.ONLY_DIR)
    modDirs.foreach(loadModMeta)
    _modMetas.foreach(pair => loadMod(pair._2))
    _mods.foreach(pair => pair._2.processJson())
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

    _modMetas += modId -> ModMeta(modId, modName, modDesc, modDependencies, modSrcDir)
  }

  private def loadMod(meta: ModMeta): Unit = {
    if (!_mods.contains(meta.id)) {
      val dependencies = meta.dependencies
      val modDir = meta.modDir
      dependencies.foreach(depId => loadMod(_modMetas(depId)))

      val mod = new Mod(meta)
      val files = ls(modDir, recursive = true, ONLY_FILE)
      files.foreach(fromFile(_).foreach(mod.loadJson(_)))
      _mods += meta.id -> mod

      val depMod = dependencies.map(_mods(_))
      mod.handleCopyFrom(depMod)
    }
  }
}
