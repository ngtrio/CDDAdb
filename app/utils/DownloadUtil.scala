package utils

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.FileIO
import play.api.Logger

import java.nio.file.Path
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * @author jaron
 *         created on 12/20/20 at 11:22 AM
 */
object DownloadUtil {

  private val log = Logger(FileUtil.getClass)

  def download(uri: String, file: String): Future[Long] = {
    import akka.http.scaladsl.model.headers.`Content-Length`
    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val httpRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = uri
    )
    val responseFuture = Http().singleRequest(httpRequest)

    responseFuture.flatMap {
      case response @ HttpResponse(StatusCodes.OK, _, _, _) =>
        val contentLength = response.headers[`Content-Length`]

        if (contentLength.nonEmpty) {
          val MB = 1024 * 1024
          val size = contentLength.head.value().toInt / MB
          log.info(s"Begin to download latest source from $uri, size: ${size}MB")
        } else {
          log.info(s"Begin to download latest source $uri, size: unknown")
        }

        val path = Path.of(file)
        // create if not exists
        path.toFile.getParentFile.mkdirs()
        path.toFile.createNewFile()

        val source = response.entity.withoutSizeLimit().dataBytes
        source.runWith(FileIO.toPath(path)).map(result => result.count)
    }
  }
}
