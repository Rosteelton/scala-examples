import cats.effect.ExitCode
import donotmodifyme.Scenario2.database.DatabaseCredentials
import monix.eval.{Task, TaskApp}
import review.Scenario2._

object App extends TaskApp {
  def run(args: List[String]): Task[ExitCode] = {
    val session = new DefaultSession[Task](new DatabaseCredentials {})
    session.getByKey("").map { result =>
      println(result)
    }.map(_ => ExitCode.Success)

  }
}
