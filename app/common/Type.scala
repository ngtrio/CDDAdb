package common

trait TypeLiteral {
  val BOOK = "book"
  val COMESTIBLE = "comestible"
  val MONSTER = "monster"
  val AMMO = "ammo"

  // 表示类型缺省
  val NONE = "none"
}

trait Type {
  type Monster
}

object Type extends TypeLiteral with Type {}
