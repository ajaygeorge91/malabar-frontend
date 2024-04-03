package models.services

import forms.CreateAssetForm
import models.{CreateNftAsset, GetWalletTransactionsResponse, ImportWalletFromPublicKey, UserPrivateWallet, UserPublicWallet, WalletSummary}

import java.util.UUID
import scala.concurrent.Future

trait WalletInfoService {

  def addPublicWalletForUser(userId: UUID, importWalletFromPublicKey: ImportWalletFromPublicKey) : Future[UserPublicWallet]
  def getPublicWalletsForUser(userId:UUID) : Future[Seq[UserPublicWallet]]
  def getWalletsFromPublicKeyForUser(userId:UUID, publicKey:String) : Future[Option[UserPublicWallet]]

  def getPublicWalletDetails(userPublicWalletId:UUID) : Future[WalletSummary]
  def getWalletTransactions(walletId:String) : Future[Seq[GetWalletTransactionsResponse]]

  def getPrivateWallet(userPrivateWalletId:UUID) : Future[UserPrivateWallet]
  def createWalletForUser(userId: UUID, walletName:String) : Future[UserPrivateWallet]
  def getPrivateWalletsForUser(userId:UUID) : Future[Seq[UserPrivateWallet]]
  def getPrivateWalletDetails(userPrivateWalletId:UUID) : Future[WalletSummary]

  def createNftAsset(userId:UUID, createNftAsset:CreateNftAsset) : Future[CreateNftAsset]
  def createAsset(userId:UUID, createAssetData:CreateAssetForm.Data) : Future[String]
  def submitCreateAsset(transaction:String, witnessSet:String) : Future[String]

}
