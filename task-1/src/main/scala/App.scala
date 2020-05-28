import design.Scenario3._

object App extends App {
  {
    for {
      user <- User("QWERTY", "QWERTY", "asdf", 1, 3500, UserType.PaidUser(1))
      recalculated <- UserLogic.runAtMidnight(user)
    } yield recalculated
  }.foreach(println)
}
