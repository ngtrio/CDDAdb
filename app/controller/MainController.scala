package controller

import javax.inject.Inject
import play.api.mvc._
import repository.MemRepository

class MainController @Inject()(cc: ControllerComponents,
                               repo: MemRepository
                              ) extends AbstractController(cc) {
  def getInfo(prefix: String, id: String): Action[AnyContent] = Action {
    val res = repo.getOne(s"$prefix:$id")
    Ok(res).as(JSON)
  }

  def listAll(tp: String): Action[AnyContent] = Action {
    val res = repo.listNameInfo(tp)
    Ok(res).as(JSON)
  }
}
