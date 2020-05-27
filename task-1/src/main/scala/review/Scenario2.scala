package review

import cats.effect.Sync
import donotmodifyme.Scenario2._
import database._
import cats.implicits._

object Scenario2 {

  trait Session[F[_]] {
    def insert(datum: Datum): F[Unit]
    def getByKey(key: String): F[Option[Datum]]
    def getAll: F[Seq[Datum]]
  }

  final class DefaultSession[F[_] : Sync](credentials: DatabaseCredentials) extends Session[F] {
    val syncF: Sync[F] = implicitly[Sync[F]]

    val connectionF: F[DatabaseConnection] =
      syncF.bracket(syncF.delay(DatabaseConnection.open(credentials)))(_.pure[F])(con => syncF.delay(con.close()))

    def insert(datum: Datum): F[Unit] = {
      connectionF.map(_.put(datum.key, datum.serializeContent))
    }

    def getByKey(key: String): F[Option[Datum]] =
      connectionF.flatMap { con =>
        Option(con.fetch(key)).traverse(bytes => syncF.fromEither(Datum.deserialize(bytes)))
      }

    def getAll: F[Seq[Datum]] = {
      connectionF.flatMap { con =>
        con.keys.toList.traverse(getByKey).map(_.flatten)
      }
    }
  }

}