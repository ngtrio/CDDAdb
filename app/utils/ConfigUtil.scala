package utils

import java.io.File

import utils.JsonUtil._

/**
 * Loading json config files
 *
 * Note: recommend to load config file (with .conf extension)
 * using play's [[com.typesafe.config.ConfigFactory]]
 */
object ConfigUtil {
  private val pathPrefix = "conf/"

  def get(key: String, configFile: String): String = {
    // By convention, json config file contains a single object
    val conf = fromFile(new File(s"$pathPrefix$configFile")).head
    getString(key)(conf)
  }
}
