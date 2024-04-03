package models.daos

import models.{UserPrivateWallet, UserPublicWallet, WalletErrorMessage}
import play.api.db.slick.DatabaseConfigProvider

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletDAOImpl @Inject()(
                               protected val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) extends WalletDAO with DAOSlick{
  import profile.api._

  def addWalletEntry(userID: UUID, walletName: String, publicKey:String, walletID:String): Future[UserPublicWallet] = {
    val userWalletId = UUID.randomUUID()
    val actions = (for {
      _ <- slickUserWalletInfos += DBUserWalletInfo(
        id= userWalletId,
        userId = userID,
        walletId = walletID,
        name = walletName,
        publicKey = publicKey
//        createdAt = ZonedDateTime.now()
      )
    } yield()).transactionally
    db.run(actions).flatMap(_ => {
      getUserWallet(userWalletId)
    })
  }

  def getUserWallet(userWalletID: UUID): Future[UserPublicWallet] = {
    val userWalletQuery = for {
      userWallet <- slickUserWalletInfos.filter(_.id === userWalletID)
    } yield (userWallet)
    db.run(userWalletQuery.result.headOption).flatMap {
      case Some(w) => Future.successful(
        UserPublicWallet(userWalletId = w.id, name = w.name,  userId = w.userId, walletId = w.walletId, publicKey = w.publicKey)
      )
      case None =>Future.failed(WalletErrorMessage("Wallet not found"))
    }
  }

  def listUserWallet(userID: UUID): Future[Seq[UserPublicWallet]] = {
    val userWalletQuery = for {
      userWallet <- slickUserWalletInfos.filter(f => f.userId === userID)
    } yield (userWallet)
    db.run(userWalletQuery.result)
      .map(f => f.map(w =>
        UserPublicWallet(userWalletId = w.id, name = w.name,  userId = w.userId, walletId = w.walletId, publicKey = w.publicKey))
      )
  }

  def getUserPrivateWallet(userWalletID: UUID): Future[UserPrivateWallet] = {
    val userWalletQuery = for {
      userWallet <- slickUserPrivateWalletInfos.filter(_.id === userWalletID)
    } yield userWallet
    db.run(userWalletQuery.result.headOption).flatMap {
      case Some(w) => Future.successful(
        UserPrivateWallet(userWalletId = w.id, name = w.name,  userId = w.userId, baseAddress = w.baseAddress, privateKey = w.privateKey)
      )
      case None =>Future.failed(WalletErrorMessage("Wallet not found"))
    }
  }

  def addPrivateWalletEntry(userID: UUID, walletName: String, privateKey:String, baseAddress:String): Future[UserPrivateWallet] = {
    val userWalletId = UUID.randomUUID()
    val actions = (for {
      _ <- slickUserPrivateWalletInfos += DBUserPrivateWalletInfo(
        id= userWalletId,
        userId = userID,
        baseAddress = baseAddress,
        name = walletName,
        privateKey = privateKey
        //        createdAt = ZonedDateTime.now()
      )
    } yield()).transactionally
    db.run(actions).flatMap(_ => {
      getUserPrivateWallet(userWalletId)
    })
  }

  def listPrivateUserWallet(userID: UUID): Future[Seq[UserPrivateWallet]] = {
    val userWalletQuery = for {
      userWallet <- slickUserPrivateWalletInfos.filter(f => f.userId === userID)
    } yield (userWallet)
    db.run(userWalletQuery.result)
      .map(f => f.map(w =>
        UserPrivateWallet(userWalletId = w.id, name = w.name,  userId = w.userId, baseAddress = w.baseAddress, privateKey = w.privateKey))
      )
  }

}
