package manager

import java.io.File

import common.{Field, Type}
import play.api.Logger
import play.api.libs.json._
import utils.FileUtil._
import utils.JsonUtil._
import utils.StringUtil.parseColor

import scala.collection.mutable.ListBuffer

abstract class BaseResourceManager extends ResourceManager {
  protected val log: Logger = Logger(this.getClass)

  protected var poPath: String = _
  protected var dataPath: List[String] = _

  //copy-from obj cache
  private[this] var cpfCache = ListBuffer[JsObject]()

  override def update(): Unit = {
    dataPath.foreach {
      path =>
        val files = ls(new File(path), recursive = true, ONLY_FILE)
        files.foreach(fromFile(_).foreach(registerJsObj(_)))
    }
    // 处理cpfCache
    //FIXME: 可以将所有json预加载，然后根据id递归处理copy-from，O(n)时间
    // 下面这个循环最坏情况为 O(n^2)，如果继承关系在文件中正序，则为O(n)
    while (cpfCache.nonEmpty) {
      cpfCache = cpfCache.filter(!registerJsObj(_))
    }
  }

  // 这里根据目前支持的type进行逐个添加，更完善后直接exclude就行了
  private val whitelist = Set[String](
    Type.MONSTER, Type.COMESTIBLE, Type.AMMO, Type.BOOK
  )

  private def registerJsObj(implicit jsObject: JsObject): Boolean = {
    val name = jsObject \ Field.NAME
    log.info(s"registering: $name")

    val tp = getField(Field.TYPE, jsObject, Type.NONE)(_.as[String]).toLowerCase

    try {
      if (!whitelist.contains(tp)) {
        false // 不注册该类型json
      } else {
        preProcess(jsObject, tp) match {
          case Some(value) =>
            val (key, obj) = value
            postProcess(key, obj)
            true
          case None =>
            cpfCache += jsObject // parent还没加载，先缓存起来后续处理
            false
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error(s"register fail, err: $e, obj: $jsObject")
        false
    }
  }

  /**
   * 预处理json的内容，比如json inheritance，同时对json进行分类
   * 本方法返回的JsObject就是此后展示层接受的数据，所以任何字段处理都必须在此处理完
   *
   * @see [[Type]]
   */
  private def preProcess(jsObject: JsObject, tp: String): Option[(List[String], JsObject)] = {
    import Type._
    import handler.HandlerImplicit._

    // 对各Type做特异性处理
    val opt = tp match {
      case MONSTER => jsObject.process[Monster]
      case _ => jsObject.process[Item]
    }

    opt.map {
      v =>
        // 处理color字段
        val pend = handleColor(v._2)
        v._1 -> pend
    }
  }

  private def handleColor(obj: JsObject): JsObject = {
    val tf = (__ \ Field.COLOR).json.update(__.read[JsString].map(str => JsArray(parseColor(str.as[String]).map(JsString))))
    obj.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(_) => obj
    }
  }

  protected def indexKey(tp: String, name: String): String = s"$tp.$name"

  //========================模板方法==================================
  /**
   * 供子类实现，子类将根据自身的索引维护方式注册索引
   *
   * @param keys 索引keys
   * @param jo   json对象
   */
  protected def postProcess(keys: List[String], jo: JsObject): Unit
}