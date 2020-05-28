package task2

import java.util.concurrent.atomic.{AtomicInteger, AtomicIntegerArray}
import java.util.Random

class MultiQueue[A >: Null] private (
    // nQueues is guaranteed to be at least 2
    nQueues: Int
    // other arguments here if needed
)(implicit ordering: Ordering[A]) {
  private val rand = new Random()
  private val locks = new AtomicIntegerArray(nQueues)
  private val queues = Array.fill(nQueues)(Queue.empty[A])
  private val sizeQ = new AtomicInteger(0)

  def isEmpty: Boolean = size == 0
  def size: Int = sizeQ.get()

  private def getRandomQueueIndex: Int = rand.nextInt(nQueues)

  @scala.annotation.tailrec
  private def findAndLockIndex: Int = {
    val index = getRandomQueueIndex
    if (locks.compareAndSet(index, 0, 1))
      index
    else findAndLockIndex
  }

  private def deleteAndUpdateCounter(index: Int): A = {
    val res = queues(index).deleteMin()
    sizeQ.incrementAndGet()
    res
  }

  private def releaseLockUnsafe(index: Int): Unit =
    locks.set(index, 0)

  def insert(element: A): Unit = {
    val index = findAndLockIndex
    queues(index).enqueue(element)
    sizeQ.incrementAndGet()
    locks.set(index, 0)
  }

  /*
   * Smallest elements (non-strictly!) first.
   */
  def deleteMin(): A = {
    val index1 = findAndLockIndex
    val index2 = findAndLockIndex

    try {
      (queues(index1).peekMin, queues(index2).peekMin) match {
        case (null, null) => null
        case (_, null)    => deleteAndUpdateCounter(index1)
        case (null, _)    => deleteAndUpdateCounter(index2)
        case (v1, v2) =>
          if (ordering.lteq(v1, v2)) {
            deleteAndUpdateCounter(index1)
          } else deleteAndUpdateCounter(index2)
      }
    } finally {
      releaseLockUnsafe(index1)
      releaseLockUnsafe(index2)
    }
  }
}

object MultiQueue {
  // You can ignore the scaling factor and the actuall amount of processors just use the given nQueues.
  def empty[A >: Null](nQueues: Int)(
      implicit ordering: Ordering[A]): MultiQueue[A] = {
    new MultiQueue(math.max(2, nQueues))
  }
}
