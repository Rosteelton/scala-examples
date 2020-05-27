package review

import donotmodifyme.Scenario1._
import monix.eval.Task
import cats.syntax.either._
import cats.syntax.applicative._
import review.Scenario1Error.ResultIsLessThanZero

sealed trait Scenario1Error
object Scenario1Error {
  case object ResultIsLessThanZero extends Scenario1Error
}

object Scenario1 {

  private def getPositiveNumber: Task[Int] =
    Task.evalAsync(blackBoxPositiveInt).flatMap {
      case i if i > 0 => i.pure[Task]
      case i =>
        Task.raiseError(
          new IllegalArgumentException(s"Negative or eq zero: $i"))
    }

  /**
    * @return the amount of calls to `blackBoxPositiveInt` needed, so that the sum of all returned values from
    *         * `blackBoxPositiveInt` would be equal to @input `total`
    */
  def process(total: Int): Task[Either[Scenario1Error, Int]] = {
    helper(total, 0)
  }

  private def helper(total: Int, n: Int): Task[Either[Scenario1Error, Int]] = {
    total match {
      case t if t < 0  => ResultIsLessThanZero.asLeft[Int].pure[Task]
      case t if t == 0 => n.asRight.pure[Task]
      case t           => getPositiveNumber.flatMap(i => helper(t - i, n + 1))
    }
  }
}
