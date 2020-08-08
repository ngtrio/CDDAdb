package handler

import common.Field._
import common.Type._
import play.api.Logger
import play.api.libs.json._
import utils.I18nUtil.{tranIdent, tranObj}
import utils.JsonUtil._

import scala.collection.mutable

object RecipeHandler extends Handler {
  private val log = Logger(RecipeHandler.getClass)
  private val prefix: String = RECIPE

  override def handle(objs: mutable.Map[String, JsObject])(implicit ctxt: HandlerContext): Unit = {
    log.debug(s"handling ${objs.size} objects, wait...")
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        var pend = fill(obj)
        pend = handleRequirement(pend)
        handleBookLearn(pend)
        handleCraft(pend)
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

  // 将recipe id加入到book json中，从而可在book查询包含的recipe
  private def handleBookLearn(obj: JsObject)(implicit ctxt: HandlerContext): Unit = {
    val books = getArray(BOOK_LEARN)(obj)
    val resItemId = getString(RESULT)(obj)
    books.value.foreach {
      book =>
        val arr = book.as[JsArray].value
        val bookId = arr(0).as[String]
        val name = if (arr.length == 3) arr(2).as[String] else ""
        val inBook = Json.arr(resItemId, name)
        ctxt.objCache(ITEM).get(bookId) match {
          case Some(value) =>
            val newObj = addToArray(RECIPES, inBook)(value)
            ctxt.objCache(ITEM)(bookId) = newObj
          case None => throw new Exception(s"book not found, id: $bookId")
        }
    }
  }

  private def handleRequirement(obj: JsObject)(implicit ctxt: HandlerContext): JsObject = {
    def handleList(field: String, arrField: JsArray, unit: Int): JsArray = {
      var newComps = JsArray()
      arrField.value.foreach {
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
                    val childComp = handleList(field, value(field).as[JsArray], multi * unit)
                    if (childComp.value.length == 1) {
                      childComp(0).as[JsArray].value.foreach {
                        elem => newAlt :+= handleMigration(elem.as[JsArray])
                      }
                    } else {
                      throw new Exception(s"this shouldn't happen")
                    }
                  case None => throw new Exception(s"requirement not found, id: $rid, json: $obj")
                }
              } else {
                newAlt :+= handleMigration(Json.arr(JsString(rid), JsNumber(multi * unit)))
              }
          }
          newComps :+= newAlt
      }
      newComps
    }

    def handleMigration(jsArray: JsArray): JsArray = {
      val id = jsArray(0)
      val item = ctxt.objCache(ITEM)(id.as[String])
      val mig = getString(REPLACE)(item)
      if (mig != "") {
        Json.arr(mig, jsArray(1))
      } else jsArray
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
              tools ++= handleList(TOOLS, getArray(TOOLS)(value), multi)
              comp ++= handleList(COMPONENTS, getArray(COMPONENTS)(value), multi)
            case None => throw new Exception(s"requirement not found, id: $rid")
          }
      }
    }

    obj ++ Json.obj(
      QUALITIES -> (getArray(QUALITIES)(obj) ++ quan),
      // ??? doc says '"tools" lists item ids of tools', but it's wrong
      // it's actually like the component field!
      TOOLS -> (handleList(TOOLS, getArray(TOOLS)(obj), 1) ++ tools),
      COMPONENTS -> (handleList(COMPONENTS, getArray(COMPONENTS)(obj), 1) ++ comp)
    )
  }

  private def handleCraft(obj: JsObject)(implicit ctxt: HandlerContext): Unit = {
    val result = getString(RESULT)(obj)
    ctxt.objCache(ITEM).get(result).foreach {
      x => ctxt.objCache(ITEM)(result) = x ++ Json.obj(CAN_CRAFT -> true)
    }
    getArray(COMPONENTS)(obj).value.foreach {
      alt =>
        alt.as[JsArray].value.foreach {
          com =>
            val comId = com.as[JsArray].value(0)
            val item = ctxt.objCache(ITEM)(comId.as[String])
            ctxt.objCache(ITEM)(comId.as[String]) = addToArray(CRAFT_TO, JsString(result))(item)
        }
    }
  }

  override def finalize(objs: mutable.Map[String, JsObject])
                       (implicit ctxt: HandlerContext): Unit = {
    objs.foreach {
      pair =>
        val (ident, obj) = pair
        val result = getString(RESULT)(obj)
        val pend = tranObj(obj, QUALITIES, TOOLS, COMPONENTS, BOOK_LEARN) ++ Json.obj(
          NAME -> tranIdent(ITEM, result)
        )
        ctxt.addIndex(
          s"$prefix:$ident" -> pend,
        )
    }
  }
}
