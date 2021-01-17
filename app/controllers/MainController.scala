package controllers

import common.WritesImplicit._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import repository.MemRepository

import javax.inject.Inject

class MainController @Inject()(cc: ControllerComponents,
                               repo: MemRepository
                              ) extends AbstractController(cc) {

  def getInfo(`type`: String, id: String): Action[AnyContent] = Action {
    val res = repo.getByTypeAndId(`type`, id)
    Ok(Json.toJson(res))
  }
}
