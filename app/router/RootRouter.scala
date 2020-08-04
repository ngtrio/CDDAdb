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
    case GET(p"/item/$tp/$name") => testCtl.getInfo(s"item:$tp", name)
    case GET(p"/$tp/$to") => testCtl.getInfo(tp, to)
    // fixme: 被第一个router拦截
    //    case GET(p"/item/$tp") => testCtl.listAll(s"item:$tp")
    case GET(p"/$tp") => testCtl.listAll(tp)
  }
}
