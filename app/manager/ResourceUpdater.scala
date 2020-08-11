package manager

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

import com.typesafe.config.ConfigFactory
import common.FileType
import org.jsoup.Jsoup
import play.api.Logger
import utils.TimeUtil.stopwatch

object ResourceUpdater {
  private val log = Logger(this.getClass)
  private val commonConf = ConfigFactory.load("common.conf")
  private val secretConf = ConfigFactory.load("secret.conf")

  private val transUri = commonConf.getString("uri.transUri")
  private val dataPath = "data/cdda.zip"
  private val dataDir = "data/cdda"
  private val transPath = "data/zh.po"

  private val httpClient = HttpClient
    .newBuilder()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .build()

  def update(): Boolean = {
    import utils.FileUtil.unzip

    val latestUri = getLatestBuildUri
    val transifexCookie = secretConf.getString("transifex-cookie")
    if (latestUri != "") {
      try {
        download(latestUri, dataPath, FileType.BINARY)
        //        download(transUri, transPath, FileType.TEXT, "cookie" -> transifexCookie)

        unzip(dataPath, dataDir)
        true
      } catch {
        case e: Exception =>
          e.printStackTrace()
          false
      }
    } else false
  }

  private def download(uri: String, path: String, fileType: Int, headers: (String, String)*): Unit = {
    import HttpResponse.BodyHandlers.{discarding, ofInputStream}

    import utils.FileUtil.writeToFile
    import utils.StringUtil.parseContentChange

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

    val gamePageUri = commonConf.getString("uri.gamePageUri")
    val doc = Jsoup.connect(gamePageUri).get
    val buildName = doc.getElementsByTag("h2").get(0).text
    log.info(s"latest build: $buildName")

    val links = doc
      .getElementsByTag("ul")
      .get(0)
      .getElementsByTag("a")
    val uris = links.asScala.map(_.attr("href"))
    uris.foldLeft("") {
      (res, elem) =>
        if (isZipExt(elem)) elem else res
    }
  }
}
