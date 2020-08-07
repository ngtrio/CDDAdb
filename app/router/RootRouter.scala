package router

import controller.MainController
import javax.inject.{Inject, Singleton}
import play.api.routing.Router.Routes
import play.api.routing._
import play.api.routing.sird._

@Singleton
class RootRouter @Inject()(testCtl: MainController)
  extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/$tp/$id") => testCtl.getInfo(tp, id)
    case GET(p"/$tp") => testCtl.listAll(tp)
  }
}
