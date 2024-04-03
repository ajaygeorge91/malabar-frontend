package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.{AuthToken, User}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

import java.time.ZonedDateTime
import java.util.UUID

trait DBTableDefinitions {

  protected val profile: JdbcProfile
  import profile.api._

  // AUTH
  class AuthTokens(tag: Tag) extends Table[AuthToken](tag, Some("auth"), "token") {
    import MyPostgresProfile.api._

    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def expiry = column[ZonedDateTime]("expiry")
    def * = (id, userId, expiry).<>(AuthToken.tupled, AuthToken.unapply)
  }

  case class DBUser(userID: UUID,
                    name: String,
                    email: Option[String],
                    avatarURL: Option[String],
                    activated: Boolean,
                    signedUpAt: ZonedDateTime)

  object DBUser {
    def toUser(u: DBUser, loginInfo: DBLoginInfo): User = models.User(userID = u.userID,
      loginInfo=LoginInfo(loginInfo.providerID,loginInfo.providerKey),
      name = u.name, email = u.email, avatarURL = u.avatarURL, activated = u.activated)
    def fromUser(u: User): DBUser = DBUser(userID = u.userID, name = u.name, email = u.email, avatarURL = u.avatarURL, activated = u.activated, signedUpAt = ZonedDateTime.now)
  }

  class Users(tag: Tag) extends Table[DBUser](tag, Some("auth"), "user") {
    import MyPostgresProfile.api._

    def id = column[UUID]("id", O.PrimaryKey)
    def name = column[String]("name")
    def email = column[Option[String]]("email")
    def avatarURL = column[Option[String]]("avatar_url")
    def activated = column[Boolean]("activated")
    def signedUpAt = column[ZonedDateTime]("signed_up_at")
    def * = (id, name, email, avatarURL, activated, signedUpAt).<>((DBUser.apply _).tupled, DBUser.unapply)
  }

  case class DBLoginInfo(id: Option[Long], providerID: String, providerKey: String)
  object DBLoginInfo {
    def fromLoginInfo(loginInfo: LoginInfo): DBLoginInfo = DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)
    def toLoginInfo(dbLoginInfo: DBLoginInfo) = LoginInfo(dbLoginInfo.providerID, dbLoginInfo.providerKey)
  }

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, Some("auth"), "login_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def * = (id.?, providerID, providerKey).<>((DBLoginInfo.apply _).tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(
    userID: UUID,
    loginInfoId: Long
  )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, Some("auth"), "user_login_info") {
    def userID = column[UUID]("user_id")
    def loginInfoId = column[Long]("login_info_id")
    def * = (userID, loginInfoId).<>(DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo(
    hasher: String,
    password: String,
    salt: Option[String],
    loginInfoId: Long
  )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, Some("auth"), "password_info") {
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[Option[String]]("salt")
    def loginInfoId = column[Long]("login_info_id")
    def * = (hasher, password, salt, loginInfoId).<>(DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  case class DBOAuth1Info(
    id: Option[Long],
    token: String,
    secret: String,
    loginInfoId: Long
  )

  class OAuth1Infos(tag: Tag) extends Table[DBOAuth1Info](tag, Some("auth"), "oauth1_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def token = column[String]("token")
    def secret = column[String]("secret")
    def loginInfoId = column[Long]("login_info_id")
    def * = (id.?, token, secret, loginInfoId).<>(DBOAuth1Info.tupled, DBOAuth1Info.unapply)
  }

  case class DBOAuth2Info(
    id: Option[Long],
    accessToken: String,
    tokenType: Option[String],
    expiresIn: Option[Int],
    refreshToken: Option[String],
    loginInfoId: Long
  )

  class OAuth2Infos(tag: Tag) extends Table[DBOAuth2Info](tag, Some("auth"), "oauth2_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def accessToken = column[String]("access_token")
    def tokenType = column[Option[String]]("token_type")
    def expiresIn = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def loginInfoId = column[Long]("login_info_id")
    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId).<>(DBOAuth2Info.tupled, DBOAuth2Info.unapply)
  }

  case class DBBearerTokenAuthenticatorInfo(
    id: String,
    lastUsedDateTime: String,
    expirationDateTime: String,
    idleTimeout: Option[Long],
    loginInfoId: String
  )

  class BearerTokenAuthenticatorInfos(tag: Tag) extends Table[DBBearerTokenAuthenticatorInfo](tag, Some("auth"), "bearer_token_authenticator_info") {
    def id = column[String]("id", O.PrimaryKey)
    def lastUsedDateTime = column[String]("last_used_date_time")
    def expirationDateTime = column[String]("expiration_date_time")
    def idleTimeout = column[Option[Long]]("idle_timeout")
    def loginInfoId = column[String]("login_info_id")
    def * = (id, lastUsedDateTime, expirationDateTime, idleTimeout, loginInfoId).<>(DBBearerTokenAuthenticatorInfo.tupled, DBBearerTokenAuthenticatorInfo.unapply)
  }


  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickAuthTokens = TableQuery[AuthTokens]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickBearerTokenAuthenticatorInfos = TableQuery[BearerTokenAuthenticatorInfos]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)



  case class DBUserWalletInfo(
                               id: UUID,
                               userId: UUID,
                               walletId: String,
                               name: String,
                               publicKey: String,
//                               createdAt: ZonedDateTime
                             )

  class UserWalletInfos(tag: Tag) extends Table[DBUserWalletInfo](tag, Some("wallet"), "user_wallet_info") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def name = column[String]("name")
    def walletId = column[String]("wallet_id")
    def public_key = column[String]("public_key")
//    def createdAt = column[ZonedDateTime]("created_at")
    def * = (id, userId, name, walletId, public_key).<>(DBUserWalletInfo.tupled, DBUserWalletInfo.unapply)
  }

  case class DBUserPrivateWalletInfo(
                               id: UUID,
                               userId: UUID,
                               baseAddress: String,
                               name: String,
                               privateKey: String,
//                               createdAt: ZonedDateTime
                             )

  class UserPrivateWalletInfos(tag: Tag) extends Table[DBUserPrivateWalletInfo](tag, Some("wallet"), "user_private_wallet_info") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def name = column[String]("name")
    def baseAddress = column[String]("base_address")
    def private_key = column[String]("private_key")
//    def createdAt = column[ZonedDateTime]("created_at")
    def * = (id, userId, name, baseAddress, private_key).<>(DBUserPrivateWalletInfo.tupled, DBUserPrivateWalletInfo.unapply)
  }

  // table query definitions
  val slickUserWalletInfos = TableQuery[UserWalletInfos]
  val slickUserPrivateWalletInfos = TableQuery[UserPrivateWalletInfos]



}
