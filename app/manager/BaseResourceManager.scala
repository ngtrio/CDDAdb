package manager

import java.io.File

import common.{Field, Type}
import handler.{CopyFromSupport, Handler}
import play.api.Logger
import play.api.libs.json._
import utils.FileUtil._
import utils.JsonUtil._

import scala.collection.mutable.ListBuffer

abstract class BaseResourceManager extends ResourceManager {
  protected val log: Logger = Logger(this.getClass)

  protected var poPath: String = _
  protected var dataPath: List[String] = _


  private[this] val handlerPipe = ListBuffer[Handler]()

  override def withHandler(handler: Handler*): this.type = {
    handlerPipe ++= handler
    this
  }

  override def update(): Unit = {
    dataPath.foreach {
      path =>
        val files = ls(new File(path), recursive = true, ONLY_FILE)
        files.foreach(fromFile(_).foreach(registerJsObj))
    }
    finish()
  }

  // 处理缓存下来的copy-from obj
  private def finish(): Unit = {
    handlerPipe.foreach {
      case handler: CopyFromSupport =>
        handler.cpfCache.foreach(registerJsObj)
        handler.clear()
      case _ =>
    }
  }

  private def registerJsObj(jsObject: JsObject): Unit = {
    val name = jsObject \ Field.NAME
    log.debug(s"registering: $name")

    try {
      preProcess(jsObject).foreach {
        value =>
          val (key, obj) = value
          postProcess(key, obj)
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(s"register fail, err: $e, obj: $jsObject")
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   * 本方法返回的JsObject就是此后展示层接受的数据，所以任何字段处理都必须在此处理完
   *
   * @see [[Type]]
   */
  private def preProcess(jsObject: JsObject): Option[(List[String], JsObject)] = {
    handlerPipe.foldLeft(Option.empty[(List[String], JsObject)]) {
      (left, right) => {
        right.handle(jsObject) match {
          case x: Some[(List[String], JsObject)] => x
          case None => left
        }
      }
    }
  }

  //========================模板方法==================================
  /**
   * 供子类实现，子类将根据自身的索引维护方式注册索引
   *
   * @param keys 索引keys
   * @param jo   json对象
   */
  protected def postProcess(keys: List[String], jo: JsObject): Unit
}