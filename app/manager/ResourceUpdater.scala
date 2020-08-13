package manager

import java.io.File
import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

import com.typesafe.config.ConfigFactory
import common.FileType
import org.jsoup.Jsoup
import play.api.Logger
import utils.TimeUtil.stopwatch

object ResourceUpdater {
  private val log = Logger(this.getClass)
  private val commonConf = ConfigFactory.load("application.conf")
  // prod.conf is the config file only on the production server
  // except transifex-cookie there is also play.http.secret.key in this file
  private val secretConf = ConfigFactory.parseFileAnySyntax(new File("/cddadb-conf/prod.conf"))

  private val transUri = commonConf.getString("transUri")
  private val dataPath = "data/cdda.zip"
  private val dataDir = "data/cdda"
  private val transPath = "data/zh.po"

  private val httpClient = HttpClient
    .newBuilder()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .build()

  def update(): Boolean = {
    import utils.FileUtil.unzip

    val transifexCookie = secretConf.getString("transifex-cookie")
    val latestUri = getLatestBuildUri
    if (latestUri != "") {
      if (download(latestUri, dataPath, FileType.BINARY)) {
        unzip(dataPath, dataDir)
        download(transUri, transPath, FileType.TEXT, "cookie" -> transifexCookie)
      } else false
    } else false
  }

  private def download(uri: String, path: String, fileType: Int, headers: (String, String)*): Boolean = {
    import HttpResponse.BodyHandlers.{discarding, ofInputStream}

    import utils.FileUtil.writeToFile
    import utils.StringUtil.parseContentChange

    retry {
      try {
        // use Range head to get the content-length
        val initRequest = genRequest(uri, headers :+ "Range" -> "bytes=0-0": _*)
        val initResponse = httpClient.send(initRequest, discarding())
        val contentChange = initResponse.headers().allValues("Content-Range")
        val downloadRequest = if (contentChange.size > 0) {
          val contentLength = parseContentChange(contentChange.get(0))
          val MB = 1024 * 1024.0
          log.info(s"downloading to $path, size: ${f"${contentLength / MB}%.2f"} MB")

          val range = s"bytes=0-$contentLength"
          genRequest(uri, headers :+ "Range" -> range: _*)
        } else {
          // if there is no content-range header, then do direct get
          log.info(s"downloading to $path, size: unknown")
          genRequest(uri)
        }
        stopwatch {
          val downloadResponse = httpClient.send(downloadRequest, ofInputStream())
          writeToFile(downloadResponse.body(), path, fileType)
        }
        true
      } catch {
        case e: Exception =>
          e.printStackTrace()
          false
      }
    }
  }

  private def genRequest(uri: String, headers: (String, String)*): HttpRequest = {
    val hds = headers.foldLeft(List[String]()) {
      (res, h) => List(h._1, h._2) ++ res
    }
    HttpRequest
      .newBuilder()
      .uri(URI.create(uri))
      .method("GET", HttpRequest.BodyPublishers.noBody())
      .headers(hds: _*)
      .build()
  }

  private def getLatestBuildUri: String = {
    import utils.StringUtil.isZipExt

    import scala.jdk.CollectionConverters._

    var res = ""
    retry {
      try {
        val gamePageUri = commonConf.getString("gamePageUri")
        val doc = Jsoup.connect(gamePageUri).get
        val buildName = doc.getElementsByTag("h2").get(0).text
        log.info(s"latest build: $buildName")

        val links = doc
          .getElementsByTag("ul")
          .get(0)
          .getElementsByTag("a")
        val uris = links.asScala.map(_.attr("href"))
        res = uris.foldLeft("") {
          (res, elem) =>
            if (isZipExt(elem)) elem else res
        }
        true
      } catch {
        case e: Exception =>
          e.printStackTrace()
          false
      }
    }
    res
  }

  private def retry(block: => Boolean): Boolean = {
    var flag = false
    for (i <- 1 to 3; if !flag) {
      if (i > 1) log.info(s"(${i - 1})retry")
      flag = block
    }
    flag
  }
}
