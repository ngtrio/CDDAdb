package common

object Field {

  // copy-from
  val RELATIVE = "relative"
  val PROPORTIONAL = "proportional"
  val EXTEND = "extend"
  val DELETE = "delete"
  val ABSTRACT = "abstract"
  val COPY_FROM = "copy-from"

  // name
  val STR = "str"
  val CTXT = "ctxt"
  val STR_SP = "str_sp"
  val STR_PL = "str_pl"

  // generic
  val ID = "id"
  val TYPE = "type"
  val NAME = "name"
  val DESCRIPTION = "description"
  val SYMBOL = "symbol"
  val COLOR = "color"

  // monster
  val HP = "hp"
  val SPEED = "speed"
  val VOLUME = "volume"
  val WEIGHT = "weight"
  val ARMOR_CUT = "armor_cut"
  val ARMOR_BASH = "armor_bash"
  val ARMOR_BULLET = "armor_bullet"
  val ARMOR_STAB = "armor_stab"
  val ARMOR_ACID = "armor_acid"
  val ARMOR_FIRE = "armor_fire"
  val MELEE_SKILL = "melee_skill"
  val MELEE_DICE = "melee_dice"
  val MELEE_DICE_SIDES = "melee_dice_sides"
  val MELEE_CUT = "melee_cut"
  val DODGE = "dodge"
  val DIFF = "diff"
  val SPECIAL_ATTACKS = "special_attacks"
  val EMIT_FIELD = "emit_field"
  val ATTACK_COST = "attack_cost"
  val MORALE = "morale"
  val AGGRESSION = "aggression"
  val VISION_DAY = "vision_day"
  val VISION_NIGHT = "vision_night"
  val DIFFICULTY = "difficulty"

  // recipe
  val RESULT = "result"
  val ID_SUFFIX = "id_suffix"
  val SKILL_USED = "skill_used"
  val BOOK_LEARN = "book_learn"
  val USING = "using"
  val QUALITIES = "qualities"
  val TOOLS = "tools"
  val COMPONENTS = "components"
  val LEVEL = "level"

  val REPLACE = "replace"
  val OBSOLETE = "obsolete"

  // new book_learn format
  val SKILL_LEVEL = "skill_level"
  val RECIPE_NAME = "recipe_name"

  // tool
  val SUB = "sub"

  // material
  val IDENT = "ident"

  // custom
  val CRAFT_TO = "craft_to"
  val CAN_CRAFT = "can_craft"
  val RECIPES = "recipes"
}
