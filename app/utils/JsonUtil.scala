package utils

import java.io.{File, FileInputStream, FileNotFoundException}

import play.api.libs.json._

object JsonUtil {

  /**
   * 返回一个json文件中的一个或多个json object
   *
   * @param file 文件路径
   * @return
   */
  def fromFile(file: File): List[JsObject] = {
    if (!file.exists)
      throw new FileNotFoundException
    try {
      val jv = Json.parse(new FileInputStream(file))
      jv match {
        case JsArray(_) => jv.as[List[JsObject]]
        case JsObject(_) => List(jv.as[JsObject])
        case _ => List.empty[JsObject] // 空文件情况
      }
    } catch {
      case _: Exception =>
        println("skipped: " + file.getName)
        List.empty[JsObject]
    }
  }

  def getField[T](field: String, jsValue: JsValue, default: T)(f: JsValue => T): T = {
    jsValue \ field match {
      case JsDefined(value) => f(value)
      case JsUndefined() => default
    }
  }

  def getString(field: String)(implicit jsValue: JsValue): String = {
    getField(field, jsValue, "")(_.as[String])
  }

  def getNumber(field: String)(implicit jsValue: JsValue): BigDecimal = {
    getField(field, jsValue, BigDecimal(0))(_.as[BigDecimal])
  }

  def getArray(field: String)(implicit jsValue: JsValue): Array[JsValue] = {
    getField(field, jsValue, Array[JsValue]())(_.as[Array[JsValue]])
  }

  def hasField(field: String, jsValue: JsValue): Boolean = {
    jsValue \ field match {
      case _: JsDefined => true
      case _: JsUndefined => false
    }
  }
}
