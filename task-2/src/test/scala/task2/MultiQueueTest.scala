package task2

import org.scalatest.{FreeSpec, MustMatchers}

class MultiQueueTest extends FreeSpec with MustMatchers {
  "q should insert and delete correctly with nQ = 2" in {
    val q = MultiQueue.empty[Integer](2)
    q.insert(0)
    q.size mustBe 1
    q.deleteMin() mustBe 0
    q.size mustBe 0
  }

  "q should return null if there is no element" in {
    val q = MultiQueue.empty[Integer](2)
    q.deleteMin() mustBe null
    q.size mustBe 0
  }

  def deleteInTightLoop(q: MultiQueue[Integer], resultSet: Set[Int]): Set[Int] =
    if (!q.isEmpty) {
      val el = q.deleteMin()
      if (el == null) deleteInTightLoop(q, resultSet) else deleteInTightLoop(q, resultSet + el)
    } else resultSet

  "q should work in a tight loop with nQ > 2" in {
    val q = MultiQueue.empty[Integer](10)
    val elements = Range(0, 20).toSet
    elements.foreach(e => q.insert(e))
    deleteInTightLoop(q, Set.empty) mustEqual elements
  }
}
