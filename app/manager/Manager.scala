package manager

import scala.collection.mutable

trait Manager {
  protected type Map[K, V] = mutable.Map[K, V]
  protected val Map: mutable.Map.type = mutable.Map
}
