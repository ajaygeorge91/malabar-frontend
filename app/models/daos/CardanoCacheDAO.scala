package models.daos

import models.{AddressesDetailsResponse, GetWalletAddressesResponse, GetWalletResponse, UserPublicWallet}

import scala.concurrent.Future

trait CardanoCacheDAO {

  def addAddressesEntry(walletId:String,getWalletAddressesResponses: Seq[GetWalletAddressesResponse], addressesDetails: Seq[AddressesDetailsResponse]):Future[Unit]
  def getAddresses(walletId: String): Future[Seq[String]]

}
