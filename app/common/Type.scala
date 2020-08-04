package common

trait TypeLiteral {
  val ITEM = "item"
  // subtype of item
  val BOOK = "book"
  val COMESTIBLE = "comestible"
  val AMMO = "ammo"
  val GENERIC = "generic"
  val GUN = "gun"
  val BIONIC_ITEM = "bionic_item"
  val TOOL = "tool"
  val ARMOR = "armor"
  val TOOL_ARMOR = "tool_armor"
  val GUNMOD = "gunmod"
  val BATTERY = "battery"
  val TOOLMOD = "toolmod"
  val ENGINE = "engine"
  val PET_ARMOR = "pet_armor"
  val WHEEL = "wheel"
  val AMMUNITION_TYPE = "ammunition_type"
  val MIGRATION = "migration" // ignore
  val EFFECT_TYPE = "effect_type" // ignore
  val MAGAZINE = "magazine"
  val TOOL_QUALITY = "tool_quality"

  val ITEM_TYPES = List(
    BOOK, COMESTIBLE, AMMO, GENERIC, GUN, BIONIC_ITEM, TOOL, ARMOR, TOOL_ARMOR,
    GUNMOD, BATTERY, TOOLMOD, ENGINE, PET_ARMOR, WHEEL, AMMUNITION_TYPE, MAGAZINE
  )

  val RECIPE = "recipe"
  val MATERIAL = "material"
  val REQUIREMENT = "requirement"
  val MONSTER = "monster"

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

object Type extends TypeLiteral with Type {
}
