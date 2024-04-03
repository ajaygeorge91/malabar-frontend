package models.services

import akka.actor.ActorSystem
import com.bloxbean.cardano.client.account.Account
import com.bloxbean.cardano.client.backend.api.BackendService
import com.bloxbean.cardano.client.common.model.Network
import forms.CreateAssetForm
import iog.psg.cardano.ApiRequestExecutor
import models._
import models.daos.{CardanoCacheDAO, WalletDAO}
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import utils.{TokenMintUtils, TokenMintUtilsExtended}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletInfoServiceImpl @Inject() (
                                        walletDAO: WalletDAO,
                                        cardanoCacheDAO: CardanoCacheDAO,
                                        network: Network,
                                        backendService:BackendService,
                                        cardanoWalletService: CardanoWalletService
                                      )(implicit ec: ExecutionContext, as:ActorSystem) extends WalletInfoService {

  def addPublicWalletForUser(userId: UUID, importWalletFromPublicKey: ImportWalletFromPublicKey) : Future[UserPublicWallet] = {
    for{
      _ <- getWalletsFromPublicKeyForUser(userId, importWalletFromPublicKey.publicKey) flatMap {
        case Some(_) =>Future.failed(WalletErrorMessage("Wallet already exists"))
        case None =>Future.successful(())
      }
      cardanoWallet <- cardanoWalletService.getOrRestoreWalletFromPublicKey(importWalletFromPublicKey.publicKey)
      _ <- addAddresseesToCache(cardanoWallet.id)
      userWallet <- walletDAO.addWalletEntry(userID = userId,walletName = importWalletFromPublicKey.walletName,publicKey = importWalletFromPublicKey.publicKey, walletId = cardanoWallet.id)
    } yield {
      userWallet
    }
  }

  def getWalletsFromPublicKeyForUser(userId:UUID, publicKey:String) : Future[Option[UserPublicWallet]] = {
    walletDAO.listUserWallet(userId).map(wallets => wallets.find(_.publicKey.equalsIgnoreCase(publicKey)))
  }

  override def getPublicWalletsForUser(userId:UUID) : Future[Seq[UserPublicWallet]] = {
    walletDAO.listUserWallet(userId:UUID)
  }

  override def getWalletTransactions(walletId:String) : Future[Seq[GetWalletTransactionsResponse]] = {
    cardanoWalletService.getTransactionsFromWalletId(walletId).flatMap {
      case Left(value) => Future.failed(value)
      case Right(value) =>
        println(value)
        Future.successful(value.sortBy(_.inserted_at.time))
    }
  }

  override def getPublicWalletDetails(userPublicWalletId:UUID) : Future[WalletSummary] = {
    implicit val requestExecutor: ApiRequestExecutor.type = ApiRequestExecutor
    for{
      userWallet <- walletDAO.getUserWallet(userPublicWalletId)
      cw <- cardanoWalletService.getOrRestoreWalletFromPublicKey(userWallet.publicKey)
//      _ <- addAddresseesToCache(cw.id)
      addresses <- cardanoCacheDAO.getAddresses(userWallet.walletId)
      wad <- cardanoWalletService.getAddressesFromWalletId(userWallet.walletId).flatMap {
        case Left(value) =>
          Future.failed(value)
        case Right(value) =>
          Future.successful(value)
      }
      _ <- Future{


        val key = "wine lizard question luxury drink face walk knife today define impose idea reduce lobster bind citizen fiber identify moral witness cupboard gown elder agent"

        val account = new Account(network,key)
        println(account.enterpriseAddress())
        println(account.baseAddress())
        println(backendService.getAddressService.getAddressInfo(account.baseAddress()))
          println(backendService.getAddressService.getAddressInfo(account.enterpriseAddress()))

        println("..........")
        println("..........")
//        println(wad)
        println("..........")
        println("..........")

        //        println(".....<addresses>.....")
//
        List("addr1q9hp4xc9hzp4gy7fqaxxc6ahkanrhdyaak8yw0ktc8ah4f03d5hhl4m92en4y3t52t60nvwg0wdm5d58cpzt6ws3yhas0v85at",
          "addr1v9hp4xc9hzp4gy7fqaxxc6ahkanrhdyaak8yw0ktc8ah4fggpcaz0",
          "addr1q8w5luy36uwswt4370zj6wrwx7e877g8pj7z465kpvx5sph3d5hhl4m92en4y3t52t60nvwg0wdm5d58cpzt6ws3yhashylkn7",
          "addr1v8w5luy36uwswt4370zj6wrwx7e877g8pj7z465kpvx5spsuus6gj",
          "addr1q8as05h7zdyw6z57k5hafusz78xtuzp8nf65prmrrhxc92h3d5hhl4m92en4y3t52t60nvwg0wdm5d58cpzt6ws3yhaswepk9s",
          "addr1v8as05h7zdyw6z57k5hafusz78xtuzp8nf65prmrrhxc92smg8ffq")
        .foreach(address => {
          val details = backendService.getAddressService.getAddressInfo(address)
          if(details.isSuccessful) {
            print(address)
            print(": ")
//            println(details.getValue.getAmount.toList.filter(_.getUnit.equalsIgnoreCase("lovelace")).map(_.getQuantity.toLong).sum)
            println(details)
          }
        })
//        println(".....</addresses>.....")

//        cw.assets.total.foreach(a => {
//          println(".....<assets>.....")
//          val asset = backendService.getAssetService.getAsset(a.policy_id+a.asset_name)
//          println(asset)
//
//          println(".....<tx>.....")
//          val initTx = backendService.getTransactionService.getTransaction(asset.getValue.getInitialMintTxHash)
//          println(initTx)
//
//          println(".....<metadata>.....")
//          val metadata = backendService.getMetadataService.getJSONMetadataByTxnHash(asset.getValue.getInitialMintTxHash)
//          println(metadata)

//        })

      }
    } yield {
//      println(".....cw.....")
//      println(cw)


      WalletSummary(
        userWalletId = userPublicWalletId,
        userId = userWallet.userId,
        name = userWallet.name,
        cardanoWalletAccountDetails = cw
      )
    }
  }

  override def getPrivateWalletDetails(userPrivateWalletId:UUID) : Future[WalletSummary] = {
    for{
      userWallet <- walletDAO.getUserPrivateWallet(userPrivateWalletId)
      cw <- cardanoWalletService.getOrRestoreWalletFromPrivateKey(userWallet.privateKey)
    } yield {
      val account = new Account(network,userWallet.privateKey)
      account.enterpriseAddress()
      val content = backendService.getAddressService.getAddressInfo(account.baseAddress())
      println(content)
      println(".....cw.....")
      println(cw)
      WalletSummary(
        userWalletId = userPrivateWalletId,
        userId = userWallet.userId,
        name = userWallet.name,
        cardanoWalletAccountDetails = cw
      )
    }
  }

  override def createWalletForUser(userId: UUID, walletName:String) : Future[UserPrivateWallet] = {
    val account = new Account(network)
    walletDAO.addPrivateWalletEntry(userId, walletName, account.mnemonic(), account.baseAddress())
  }

  override def getPrivateWalletsForUser(userId:UUID) : Future[Seq[UserPrivateWallet]] = {
    walletDAO.listPrivateUserWallet(userId:UUID)
  }

  override def getPrivateWallet(userPrivateWalletId:UUID) : Future[UserPrivateWallet] = {
    walletDAO.getUserPrivateWallet(userPrivateWalletId)
  }

  override def createNftAsset(userId:UUID, createNftAsset:CreateNftAsset) : Future[CreateNftAsset] = {
//    getPrivateWalletsForUser(userId).flatMap(_.headOption match {
//      case Some(wallet) =>
//        new TokenMintUtils(network,backendService).paymentTransaction(
//          wallet.privateKey,
//          receiverAddress = None
//        )
//        new TokenMintUtilsExtended(network,backendService).mintToken(
//          receiverAddress = None,
//          createNftAsset
//        )
        Future.successful(createNftAsset)
//      case None =>
//        Future.failed(new RuntimeException("no private wallet found"))
//    })
  }

  override def createAsset(userId:UUID, createAssetData:CreateAssetForm.Data) : Future[String] = {
    val asset = CreateNftAsset(createAssetData.description,
                                createAssetData.name,
                                createAssetData.image,
                                createAssetData.author)
    val transactionToSign = new TokenMintUtilsExtended(network,backendService).getHalfSignedTxString(
      receiverAddress = createAssetData.addresses.split(",").toSeq,
      asset
    )
    Future.successful(transactionToSign)
  }

  override def submitCreateAsset(transaction:String, witnessSet:String) : Future[String] ={
    val transactionId = new TokenMintUtilsExtended(network,backendService).submitTxn(
      haflSignedTxnSentToUser = transaction,
      signedWitnessSetFromBrowser = witnessSet
    )
    Future.successful(transactionId)
  }

  private def addAddresseesToCache(id: String): Future[Unit] = {
    for{
      addresses <- cardanoWalletService.getAddressesFromWalletId(id).flatMap {
        case Left(value) =>
          Future.failed(value)
        case Right(value) =>
          Future.successful(value)
      }
      addressDetails <- Future.sequence(addresses.map(address => cardanoWalletService.inspectAddress(address.id).flatMap {
        case Left(value) =>
          Future.successful(None)
        case Right(value) =>
          Future.successful(Some(value))
      }))
      _ <- cardanoCacheDAO.addAddressesEntry(id, addresses, addressDetails.flatten)
    } yield ()
  }


}
