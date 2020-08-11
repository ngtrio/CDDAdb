package utils

import play.api.Logger

object TimeUtil {
  private val log = Logger(TimeUtil.getClass)

  def stopwatch[T](exec: => T): T = {
    val start = System.currentTimeMillis
    val res = exec
    val stop = System.currentTimeMillis
    log.info(f"Done! Use: ${(stop - start) / 1000.0}%.2fs")
    res
  }
}
