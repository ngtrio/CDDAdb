package repository

import common.Result
import manager.ModCache
import play.api.Logger
import play.api.libs.json.JsObject

import javax.inject.{Inject, Singleton}

@Singleton
class MemRepository @Inject()(cache: ModCache) extends Repository {
  private val log = Logger(this.getClass)

  override def getByTypeAndId(`type`: String, id: String): List[Result] = {
    val mods = cache.mods
    mods.keys
      .map(modId => modId -> mods(modId).search(`type`, id))
      .foldLeft(List[Result]()) {
        (res, opt) =>
          opt._2 match {
            case Some(value) =>
              res :+ Result(opt._1, value)
            case None =>
              res
          }
      }
  }
}
