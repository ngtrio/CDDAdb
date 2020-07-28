package common

trait TypeLiteral {
  val ITEM = "item"

  // subtype of item
  val BOOK = "book"
  val COMESTIBLE = "comestible"
  val AMMO = "ammo"

  val RECIPE = "recipe"


  val MONSTER = "monster"

  // exclude types
  val EFFECT_TYPE = "effect_type" // name字段在翻译时格式不兼容，并且有空串
  val MIGRATION = "migration" // 无用
  val TALK_TOPIC = "talk_topic" // id字段为数组
  val OVERMAP_TERRAIN = "overmap_terrain" // id字段为数组
  val EVENT_STATISTIC = "event_statistic"

  // 表示类型缺省
  val NONE = "none"
}

trait Type {
  type Monster
  type Item
  type Recipe
}

object Type extends TypeLiteral with Type {}
