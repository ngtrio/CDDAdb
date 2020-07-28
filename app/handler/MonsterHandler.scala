package handler

import common.Field._
import common.Type.Monster
import manager.TransManager
import play.api.libs.json.{JsArray, JsObject, Json}
import utils.JsonUtil._

object MonsterHandler extends Handler[Monster] {
  override def handle(obj: JsObject): JsObject = {
    val pend = fill(obj)
    val diff = difficulty(pend)
    val vol = volume(pend)
    pend ++ Json.obj(
      DIFFICULTY -> diff,
      VOLUME -> vol
    )
  }

  private def difficulty(implicit obj: JsObject): String = {
    val mSkl = getNumber(MELEE_SKILL)
    val mDice = getNumber(MELEE_DICE)
    val mCut = getNumber(MELEE_CUT)
    val mSide = getNumber(MELEE_DICE_SIDES)
    val dodge = getNumber(DODGE)
    val aBash = getNumber(ARMOR_BASH)
    val aCut = getNumber(ARMOR_CUT)
    val diff = getNumber(DIFF)
    val sAttack = getArray(SPECIAL_ATTACKS)
    val eFields = getArray(EMIT_FIELD)
    val hp = getNumber(HP)
    val speed = getNumber(SPEED)
    val aCost = getNumber(ATTACK_COST)
    val morale = getNumber(MORALE)
    val aggression = getNumber(AGGRESSION)
    val visionD = getNumber(VISION_DAY)
    val visionN = getNumber(VISION_NIGHT)

    val value = ((mSkl + 1) * mDice * (mCut + mSide) * 0.04 + (dodge + 1) *
      (3 + aBash + aCut) * 0.04 + (diff + sAttack.length + 8 * eFields.length)) *
      ((hp + speed - aCost + (morale + aggression) * 0.1) * 0.01 + (visionD + 2 * visionN) * 0.01)
    val level = difLevel(value)
    s"$value($level)"
  }

  private def difLevel(difficulty: BigDecimal): String = {
    val str = difficulty match {
      case x if x < 3 => tran("<color_light_gray>Minimal threat.</color>")
      case x if x < 10 => tran("<color_light_gray>Mildly dangerous.</color>")
      case x if x < 20 => tran("<color_light_red>Dangerous.</color>")
      case x if x < 30 => tran("<color_red>Very dangerous.</color>")
      case x if x < 50 => tran("<color_red>Extremely dangerous.</color>")
      case _ => tran("<color_red>Fatally dangerous!</color>")
    }
    str.replaceAll("<.*?>", "")
  }

  private def volume(implicit obj: JsObject): String = {
    val vol = getString(VOLUME)
    val arr = vol.split(" ")
    // 如果是 copy-from json，那么可能volume已经处理好，所以得判断一下
    if (arr.length == 2) {
      val value = if (arr(1) == "ml") arr(0).toInt / 1000.0 else arr(0).toDouble

      val desc = value match {
        case x if x <= 7.5 => tran("tiny", "size adj")
        case x if x <= 46.25 => tran("small", "size adj")
        case x if x <= 77.5 => tran("medium", "size adj")
        case x if x <= 483.75 => tran("large", "size adj")
        case _ => tran("huge", "size adj")
      }
      s"${value}L($desc)"
    } else {
      vol
    }
  }

  private def tran(toTran: String, ctxt: String = "")(implicit trans: TransManager.type) =
    trans.get(toTran, ctxt)

  private def fill(obj: JsObject): JsObject = {
    // default values see https://github.com/CleverRaven/Cataclysm-DDA/blob/master/src/mtype.h
    val toFill = Json.obj(
      ARMOR_CUT -> 0,
      ARMOR_BASH -> 0,
      ARMOR_BULLET -> 0,
      ARMOR_STAB -> 0,
      ARMOR_ACID -> 0,
      ARMOR_FIRE -> 0,
      MELEE_SKILL -> 0,
      MELEE_DICE -> 0,
      MELEE_DICE_SIDES -> 0,
      MELEE_CUT -> 0,
      DODGE -> 0,
      DIFF -> 0,
      SPECIAL_ATTACKS -> Array[JsArray](),
      EMIT_FIELD -> Array[JsObject](),
      ATTACK_COST -> 100,
      MORALE -> 0,
      AGGRESSION -> 0,
      VISION_DAY -> 40,
      VISION_NIGHT -> 1
    )
    toFill ++ obj
  }
}