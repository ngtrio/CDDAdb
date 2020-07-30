package controller

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import repository.MemRepository

@Singleton
class MainController @Inject()(cc: ControllerComponents,
                               repo: MemRepository
                              ) extends AbstractController(cc) {
  def getInfo(prefix: String, name: String): Action[AnyContent] = Action {
    val res = repo.getOne(s"$prefix.$name")
    Ok(res).as(JSON)
  }

  def listAll(tp: String): Action[AnyContent] = Action {
    val res = repo.listNameInfo(tp)
    Ok(res).as(JSON)
  }
}
