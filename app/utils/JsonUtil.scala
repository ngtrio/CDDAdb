package utils

import java.io.{File, FileInputStream, FileNotFoundException}

import common.{Field, Type}
import play.api.Logger
import play.api.libs.json._

object JsonUtil {

  private val log = Logger(this.getClass)

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
        log.info("skipped: " + file.getName)
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
    getField(field, jsValue, "") {
      case JsString(value) => value
      case _ => throw new Exception(s"field: $field is not a string, json: $jsValue")
    }
  }

  def getNumber(field: String)(implicit jsValue: JsValue): BigDecimal = {
    getField(field, jsValue, BigDecimal(0)) {
      case JsNumber(value) => value
      case _ => throw new Exception(s"field: $field is not a number")
    }
  }

  def getArray(field: String)(implicit jsValue: JsValue): Array[JsValue] = {
    getField(field, jsValue, Array[JsValue]()) {
      case JsArray(value) => value.toArray
      case _ => throw new Exception(s"field: $field is not an array")
    }
  }

  def hasField(field: String)(implicit jsValue: JsValue): Boolean = {
    jsValue \ field match {
      case _: JsDefined => true
      case _: JsUndefined => false
    }
  }

  def updateField[T <: JsValue](path: JsPath, jsValue: JsValue, value: T): JsValue = {
    val tf = path.json.update(__.json.put(value))
    jsValue.transform(tf) match {
      case JsSuccess(value, _) => value
      case JsError(_) => jsValue
    }
  }

  def getIdent(tp: String)(implicit jsObject: JsObject): String = {
    val abstr = getString(Field.ABSTRACT)
    if (abstr == "") {
      tp match {
        case Type.RECIPE =>
          if (hasField(Field.ID_SUFFIX)) {
            s"${getString(Field.ID)}_${getString(Field.ID_SUFFIX)}"
          } else {
            s"${getString(Field.ID)}"
          }
        case Type.MATERIAL =>
          s"${getString(Field.IDENT)}"
        case _ =>
          s"${getString(Field.ID)}"
      }
    } else s"$abstr"
  }
}
