package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.db.slick.DatabaseConfigProvider

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * Give access to the user object.
 */
class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends UserDAO with DAOSlick {
  import profile.api._

  def find(loginInfo: LoginInfo): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.userID)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(userID = user.userID, loginInfo=LoginInfo(loginInfo.providerID,loginInfo.providerKey),
          name = user.name, email = user.email, avatarURL = user.avatarURL, activated = user.activated)
      }
    }
  }

  def find(userID: UUID): Future[Option[User]] = {
    val query = slickUsers.filter(_.id === userID)

    db.run(query.result.headOption).flatMap { userOption =>
      val loginInfoQuery = for {
        userLoginInfo <- slickUserLoginInfos.filter(_.userID === userID)
        loginInfo <- slickLoginInfos.filter(_.id === userLoginInfo.loginInfoId)
      } yield loginInfo

      db.run(loginInfoQuery.result.headOption).map(loginOpt => {
        for {
          login <- loginOpt
          user <- userOption
        } yield {
          DBUser.toUser(user, login)
        }
      })
    }
  }

  def save(user: User): Future[User] = {
    // combine database actions to be run sequentially
    val dbUser = DBUser(user.userID, user.name, user.email, user.avatarURL, user.activated, ZonedDateTime.now())
    val actions = (for {
      _ <- slickUsers.insertOrUpdate(dbUser)
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }

}
