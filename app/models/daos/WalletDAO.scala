package models.daos

import models.{UserPrivateWallet, UserPublicWallet}

import java.util.UUID
import scala.concurrent.Future

trait WalletDAO {
  def addWalletEntry(userID: UUID, walletName: String, publicKey:String, walletId:String): Future[UserPublicWallet]
  def listUserWallet(userID: UUID): Future[Seq[UserPublicWallet]]
  def getUserWallet(userWalletID: UUID): Future[UserPublicWallet]

  def addPrivateWalletEntry(userID: UUID, walletName: String, privateKey:String, baseAddress:String): Future[UserPrivateWallet]
  def listPrivateUserWallet(userID: UUID): Future[Seq[UserPrivateWallet]]
  def getUserPrivateWallet(userWalletID: UUID): Future[UserPrivateWallet]

}
