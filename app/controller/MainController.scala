package controller

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import repository.AllInOneRepository

@Singleton
class MainController @Inject()(cc: ControllerComponents,
                               repo: AllInOneRepository
                              ) extends AbstractController(cc) {
  def getInfo(tp: String, name: String): Action[AnyContent] = Action {
    val res = repo.getOne(tp, name)
    Ok(res).as(JSON)
  }

  def listAll(tp: String): Action[AnyContent] = Action {
    val res = repo.listNameInfo(tp)
    Ok(res).as(JSON)
  }
}
