package task1.modifyme

import spire.ClassTag
import metal.syntax._

/**
 * Stores the distances (of type `W) to the labels `A`.
 **/
abstract class Distances[A, W] {
  /*
   * Retrieves the stored distance `W` at label `W` or Infinity[W] if not reached.
   */
  def distanceAt(a: A): W
  /*
   * Updates the distance for label `A` with the value `W`
   */
  def updated(label: A, value: W): Unit
}

object Distances {
  def empty(implicit ev: Infinity[Int]): Distances[Int, Int] = new MapDistances
}

class MapDistances[A, W](implicit ev: Infinity[W], cta: ClassTag[A], ctw: ClassTag[W]) extends Distances[A, W] {
  var underlying = metal.mutable.HashMap[A, W]()

  def distanceAt(a: A): W = {
    underlying.getOrElse(a, Infinity[W])
  }

  def updated(label: A, value: W): Unit = {
    underlying.update(label, value)
  }
}
