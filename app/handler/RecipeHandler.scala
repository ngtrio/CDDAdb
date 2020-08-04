package handler

import common.Field._
import common.Type._
import play.api.Logger
import play.api.libs.json._
import utils.I18nUtil.tranObj
import utils.JsonUtil._

import scala.collection.mutable

object RecipeHandler extends Handler {
  private val log = Logger(RecipeHandler.getClass)
  private var prefix: String = RECIPE

  override def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    log.debug(s"handling ${objs.size} objects, wait...")
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        var pend = fill(obj)
        pend = handleRequirement(pend)
        handleBookLearn(pend)
        objs(ident) = pend
    }
  }

  private def fill(obj: JsObject): JsObject = {
    val toFill = Json.obj(
      QUALITIES -> JsArray(),
      TOOLS -> JsArray(),
      COMPONENTS -> JsArray()
    )
    toFill ++ obj
  }

  // 将recipe name加入到book json中，从而可在book查询包含的recipe
  private def handleBookLearn(obj: JsObject)(implicit ctxt: HandlerContext): Unit = {
    val books = getArray(BOOK_LEARN)(obj)
    val resItemId = getString(RESULT)(obj)
    books.value.foreach {
      book =>
        val arr = book.as[JsArray].value
        val bookId = arr(0).as[String]
        val inBook = if (arr.length == 3) {
          arr(2)
        } else {
          ctxt.objCache(ITEM).get(resItemId) match {
            case Some(value) => value(NAME)
            case None => throw new Exception(s"item not found, id: $resItemId")
          }
        }
        ctxt.objCache(ITEM).get(bookId) match {
          case Some(value) =>
            val newObj = addToArray(RECIPES, inBook)(value)
            ctxt.objCache(ITEM)(bookId) = newObj
          case None => throw new Exception(s"book not found, id: $bookId")
        }
    }
  }

  private def handleRequirement(obj: JsObject)(implicit ctxt: HandlerContext): JsObject = {
    def handleComponent(comps: JsArray, unit: Int): JsArray = {
      var newComps = JsArray()
      comps.value.foreach {
        alt =>
          var newAlt = JsArray()
          alt.as[JsArray].value.foreach {
            igre =>
              val arr = igre.as[JsArray].value
              val rid = arr(0).as[String]
              val multi = arr(1).as[Int]
              if (arr.length == 3 && arr(2).as[String] == "LIST") {
                ctxt.objCache(REQUIREMENT).get(rid) match {
                  case Some(value) =>
                    val childComp = handleComponent(value(COMPONENTS).as[JsArray], multi * unit)
                    if (childComp.value.length == 1) {
                      newAlt ++= childComp.value(0).as[JsArray]
                    } else {
                      throw new Exception(s"this shouldn't happen")
                    }
                  case None => throw new Exception(s"requirement not found, id: $rid")
                }
              } else {
                newAlt :+= JsArray(List(JsString(rid), JsNumber(multi * unit)))
              }
          }
          newComps :+= newAlt
      }
      newComps
    }

    var quan = JsArray()
    var tools = JsArray()
    var comp = JsArray()
    if (hasField(USING)(obj)) {
      getArray(USING)(obj).value.foreach {
        elem =>
          val arr = elem.as[JsArray].value
          val rid = arr(0).as[String]
          val multi = arr(1).as[Int]
          ctxt.objCache(REQUIREMENT).get(rid) match {
            case Some(value) =>
              quan ++= getArray(QUALITIES)(value)
              tools ++= getArray(TOOLS)(value)
              comp ++= handleComponent(getArray(COMPONENTS)(value), multi)
            case None => throw new Exception(s"requirement not found, id: $rid")
          }

      }
    }

    obj ++ Json.obj(
      QUALITIES -> (getArray(QUALITIES)(obj) ++ quan),
      TOOLS -> (getArray(TOOLS)(obj) ++ tools),
      COMPONENTS -> (handleComponent(getArray(COMPONENTS)(obj), 1) ++ comp)
    )
  }

  private def handleCraft(obj: JsObject)(implicit ctxt: HandlerContext): Unit = {

  }

  override def finalize(objs: mutable.Map[String, JsObject])
                       (implicit ctxt: HandlerContext): Unit = {
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        val pend = tranObj(obj, RESULT, QUALITIES, TOOLS, COMPONENTS)
        //        val name = getString(RESULT)(pend)
        ctxt.addIndex(
          s"$prefix:$ident" -> pend,
          //          s"$prefix:$name" -> JsString(s"$prefix:$ident")
        )
    }
  }
}