package models.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import com.google.inject.name.Named
import models.{AddressesDetailsResponse, CardanoErrorResponse, CreateWalletFromPrivateKeyRequest, CreateWalletFromPublicKeyRequest, GetWalletAddressesResponse, GetWalletResponse, GetWalletTransactionsResponse}
import play.api.libs.json.{Json, Reads}

import javax.inject.Inject
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class CardanoWalletService @Inject()(
                                       @Named("cardano-wallet-host") host:String
                                     )(implicit val actorSystem: ActorSystem) {

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher


  def getOrRestoreWalletFromPublicKey(publicKey:String): Future[GetWalletResponse] = {
    for{
      cw <- getWalletFromPublicKey(
        CreateWalletFromPublicKeyRequest(name = "testing",account_public_key = publicKey)
      )
      wallet <- manageGetWalletResponse(cw)
    } yield {
      wallet
    }
  }

  def getOrRestoreWalletFromPrivateKey(mnemonic:String): Future[GetWalletResponse] = {
    for{
      cw <- getWalletFromPrivateKey(
        CreateWalletFromPrivateKeyRequest(name = "testing", mnemonic_sentence = mnemonic.split(" ").toSeq, passphrase = "password12345")
      )
      wallet <- manageGetWalletResponse(cw)
    } yield {
      wallet
    }
  }

  private def manageGetWalletResponse(resp:Either[CardanoErrorResponse, GetWalletResponse]): Future[GetWalletResponse] = {
    resp match {
      case Left(value) =>
        println(value)
        if (value.code.equalsIgnoreCase("wallet_already_exists")) {
          val walletId = value.message
            .stripPrefix("This operation would yield a wallet with the following id:")
            .stripSuffix("However, I already know of a wallet with this id.")
            .trim
          getWalletFromWalletId(walletId).flatMap {
            case Left(value) =>
              Future.failed(value)
            case Right(value) =>
              Future.successful(value)
          }
        } else {
          Future.failed(value)
        }
      case Right(value) =>
        Future.successful(value)
    }
  }

  def getWalletFromWalletId(walletId:String): Future[Either[CardanoErrorResponse, GetWalletResponse]] = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = host+s"wallets/$walletId"
    )
    response[GetWalletResponse](request)
  }

  def getWalletFromPrivateKey(requestObj:CreateWalletFromPrivateKeyRequest): Future[Either[CardanoErrorResponse, GetWalletResponse]] = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = host+"wallets",
      entity  = HttpEntity(ContentTypes.`application/json`, Json.toJson(requestObj).toString())
    )
    response[GetWalletResponse](request)
  }

  def getWalletFromPublicKey(requestObj:CreateWalletFromPublicKeyRequest): Future[Either[CardanoErrorResponse, GetWalletResponse]] = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = host+"wallets",
      entity  = HttpEntity(ContentTypes.`application/json`, Json.toJson(requestObj).toString())
    )
    response[GetWalletResponse](request)
  }

  def getAddressesFromWalletId(walletId:String, showAll:Boolean = true): Future[Either[CardanoErrorResponse, Seq[GetWalletAddressesResponse]]] = {
    val uri = if(showAll) host+s"wallets/$walletId/addresses" else host+s"wallets/$walletId/addresses?state=used"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri
    )
    response[Seq[GetWalletAddressesResponse]](request)
  }

  def inspectAddress(address:String): Future[Either[CardanoErrorResponse, AddressesDetailsResponse]] = {
    val uri = host+s"addresses/$address"
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = uri
    )
    response[AddressesDetailsResponse](request)
  }

  def getTransactionsFromWalletId(walletId:String): Future[Either[CardanoErrorResponse, Seq[GetWalletTransactionsResponse]]] = {
    val request = HttpRequest(
      method = HttpMethods.GET,
      uri = host+s"wallets/$walletId/transactions"
    )
    response[Seq[GetWalletTransactionsResponse]](request)
  }

  private def response[D](request:HttpRequest)(implicit f:Reads[D]):Future[Either[CardanoErrorResponse, D]] = {
    Http().singleRequest(request)
      .transformWith {
        case Failure(exception) => Future.failed(exception)
        case Success(resp) =>
          println(resp)
          val respBody = resp.entity.dataBytes.map(_.utf8String).runWith(Sink.seq).map(f => Json.parse(f.mkString))

          if(resp.status.isSuccess()) {
            respBody.flatMap(r => {
              println(r)
              Json.fromJson[D](r)
                .fold(
                  invalid => Future.failed(new RuntimeException(invalid.toString()))
                  ,valid => Future.successful(Right(valid))
                )
            })
          }else{
            respBody.flatMap(r => {
              println("r_______")
              println(r)
            Json.fromJson[CardanoErrorResponse](r)
              .fold(
                invalid => {
                  println("invalid")
                  println(invalid.toString())
                  Future.failed(new RuntimeException(invalid.toString()))
                }
                ,valid => {
                  Future.successful(Left(valid))
                }
              )
            })
          }
      }
  }

}
