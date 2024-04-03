package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.Inject
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

/**
 * The DAO to store the Bearer Token information.
 */
class AuthenticatorRepositoryDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends AuthenticatorRepository[BearerTokenAuthenticator] with DAOSlick {

  import profile.api._

  private def bearerTokenAuthenticatorInfoSubQuery(id:String) =
    slickBearerTokenAuthenticatorInfos.filter(_.id  === id)

  override def find(id: String): Future[Option[BearerTokenAuthenticator]] = {
    db.run(bearerTokenAuthenticatorInfoSubQuery(id).result.headOption).map { valueO =>
      valueO.map(value => DBBearerTokenAuthenticatorInfoUtils.fromDBBearerTokenAuthenticatorInfo(value))
    }
  }

  override def add(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    db.run(
      slickBearerTokenAuthenticatorInfos += DBBearerTokenAuthenticatorInfoUtils.toDBBearerTokenAuthenticatorInfo(authenticator)
    ).map(_ => authenticator)
  }

  override def update(authenticator: BearerTokenAuthenticator): Future[BearerTokenAuthenticator] = {
    db.run(
      bearerTokenAuthenticatorInfoSubQuery(authenticator.id).update(
        DBBearerTokenAuthenticatorInfoUtils.toDBBearerTokenAuthenticatorInfo(authenticator)
      )
    ).map(_ => authenticator)
  }

  override def remove(id: String): Future[Unit] = {
    db.run(bearerTokenAuthenticatorInfoSubQuery(id).delete).map(_ => ())
  }

  object DBBearerTokenAuthenticatorInfoUtils  {
    def toDBBearerTokenAuthenticatorInfo(bearerTokenAuthenticator:BearerTokenAuthenticator):DBBearerTokenAuthenticatorInfo = {
      DBBearerTokenAuthenticatorInfo(
        id = bearerTokenAuthenticator.id,
        lastUsedDateTime = bearerTokenAuthenticator.lastUsedDateTime.toString,
        expirationDateTime = bearerTokenAuthenticator.expirationDateTime.toString,
        idleTimeout = bearerTokenAuthenticator.idleTimeout.map(_.toSeconds),
        loginInfoId = bearerTokenAuthenticator.loginInfo.providerID+":"+bearerTokenAuthenticator.loginInfo.providerKey
      )
    }

    def fromDBBearerTokenAuthenticatorInfo(dBBearerTokenAuthenticatorInfo:DBBearerTokenAuthenticatorInfo):BearerTokenAuthenticator = {
      BearerTokenAuthenticator(id = dBBearerTokenAuthenticatorInfo.id,
        loginInfo = LoginInfo(
          dBBearerTokenAuthenticatorInfo.loginInfoId.split(":").head,
          dBBearerTokenAuthenticatorInfo.loginInfoId.split(":").last),
        lastUsedDateTime= DateTime.parse(dBBearerTokenAuthenticatorInfo.lastUsedDateTime),
        expirationDateTime = DateTime.parse(dBBearerTokenAuthenticatorInfo.expirationDateTime),
        idleTimeout = dBBearerTokenAuthenticatorInfo.idleTimeout.map(timeout => FiniteDuration(timeout, SECONDS))
      )
    }
  }

}
