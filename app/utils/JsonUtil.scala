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

  def getArray(field: String)(implicit jsValue: JsValue): JsArray = {
    getField(field, jsValue, JsArray()) {
      case x: JsArray => x
      case _ => throw new Exception(s"field: $field is not an array")
    }
  }

  def hasField(field: String)(implicit jsValue: JsValue): Boolean = {
    jsValue \ field match {
      case _: JsDefined => true
      case _: JsUndefined => false
    }
  }

  def transform[T <: JsValue](reads: Reads[T], jsValue: T): T = {
    jsValue.transform(reads) match {
      case JsSuccess(value, _) => value
      case JsError(_) => jsValue
    }
  }

  def addToArray(field: String, values: JsValue*)(implicit obj: JsObject): JsObject = {
    val arr = obj(field).as[JsArray]
    obj ++ Json.obj(
      field -> JsArray(arr.value ++ values)
    )
  }


  def getIdent(tp: String)(implicit jsValue: JsValue): Either[String, List[String]] = {
    val abstr = getString(Field.ABSTRACT)
    if (abstr == "") {
      tp match {
        case Type.RECIPE =>
          // see https://github.com/CleverRaven/Cataclysm-DDA/blob/30ffa2af1a1da178f3f328b54a366d60095967e4/src/recipe.cpp#L264
          if (hasField(Field.ID_SUFFIX)) {
            Left(s"${getString(Field.RESULT)}_${getString(Field.ID_SUFFIX)}")
          } else {
            Left(s"${getString(Field.RESULT)}")
          }
        case Type.MATERIAL =>
          Left(s"${getString(Field.IDENT)}")
        case Type.MIGRATION =>
          (jsValue \ Field.ID).get match {
            case x: JsArray => Right(x.value.map(_.as[String]).toList)
            case x: JsString => Left(x.as[String])
            case _ => throw new Exception("this should not happen")
          }
        case _ =>
          Left(s"${getString(Field.ID)}")
      }
    } else Left(s"$abstr")
  }
}
