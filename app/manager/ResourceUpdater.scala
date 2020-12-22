package manager

import com.typesafe.config.ConfigFactory
import play.api.Logger

import java.util.concurrent.{TimeUnit, TimeoutException}
import scala.concurrent.Await

object ResourceUpdater {
  private val log = Logger(ResourceUpdater.getClass)

  private val commonConf = ConfigFactory.load("application.conf")
  private val originUri = commonConf.getString("originUri")
  private val proxyHost = commonConf.getStringList("proxyHost")
  private val dataPath = commonConf.getString("dataPath")
  private val latestPath = commonConf.getString("latestPath")


  def update(): Boolean = {
    import utils.DownloadUtil.download
    import utils.FileUtil.unzip

    import scala.concurrent.duration._

    var success = false
    val retry = proxyHost.size + 1
    val destFile = s"$dataPath/cdda.zip"

    var i = 0
    while (!success && i < retry) {
      val uri = if (i == retry - 1) {
        originUri
      } else {
        originUri.replace("https://github.com", proxyHost.get(i))
      }

      success = {
        try {
          val dl = download(uri, destFile)
          Await.result(dl, Duration(10, TimeUnit.MINUTES)) > 0 && unzip(destFile, latestPath)
        } catch {
          case _: TimeoutException =>
            log.info("Download takes too much time, exit..")
            false
        }
      }

      i += 1
    }

    success
  }
}