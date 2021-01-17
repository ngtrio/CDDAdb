package repository

import common.Result

trait Repository {
  def getByTypeAndId(`type`: String, id: String): List[Result]
}
