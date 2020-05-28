package design

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.string.MatchesRegex

object Scenario3 {
  /**
   * Write a class to represent and create a user
   *
   * - User has a name/surname
   * - User has a username
   * - Username can only consist of characters "[a-z][A-Z][0-9]-._"
   * - User has a level
   * - User starts from level 0 and can only increase.
   * - User has experience
   * - User gets experience each time he posts or is reposted.
   * - The experience transfers to levels on midnight each day
   * - The experience can't ever be negative
   * - An user is either a free user or a paid user.
   * - A free user has a limit to the amount of posts he can write per day.
   * - A paid user has a counter of the remaining paid days
   */
  sealed trait UserType

  object UserType {

    final case class FreeUser(postLimit: Int) extends UserType

    final case class PaidUser(daysRemained: Int) extends UserType

    def recalculate(userType: UserType): UserType = {
      userType match {
        case FreeUser(postLimit) if postLimit < 3 => FreeUser(3)
        case PaidUser(daysRemained) if daysRemained <= 1 => FreeUser(3)
        case PaidUser(daysRemained) => PaidUser(daysRemained - 1)
      }
    }
  }

  type Name = String Refined NonEmpty
  type Surname = String Refined NonEmpty
  type UserName = String Refined MatchesRegex[W.`"[a-zA-Z0-9-._]+"`.T]
  type Level = Int Refined NonNegative
  type Experience = Int Refined NonNegative

  //we don't use `Refined` a lot in prod because of the compilation time
  // we use smart constructors
  final case class User private(name: Name,
                                surname: Surname,
                                userName: UserName,
                                level: Level,
                                experience: Experience,
                                userType: UserType) {
    def convertExperienceToLevel: Either[String, User] = {
      for {
        newLevel <- refineV[NonNegative](level.value + experience.value / 1000)
        newExp <- refineV[NonNegative](experience.value % 1000)
      } yield copy(level = newLevel, experience = newExp)
    }
  }

  object User {
    def apply(name: String,
              surname: String,
              userName: String,
              level: Int,
              experience: Int,
              userType: UserType): Either[String, User] =
      for {
        nameR <- refineV[NonEmpty](name)
        surnameR <- refineV[NonEmpty](surname)
        userNameR <- refineV[MatchesRegex[W.`"[a-zA-Z0-9-._]+"`.T]](userName)
        levelR <- refineV[NonNegative](level)
        experienceR <- refineV[NonNegative](experience)
      } yield
        new User(nameR, surnameR, userNameR, levelR, experienceR, userType)
  }

  object UserLogic {
    def runAtMidnight(user: User): Either[String, User] = {
      user.convertExperienceToLevel.map(u =>
        u.copy(userType = UserType.recalculate(u.userType)))
    }
  }
}
