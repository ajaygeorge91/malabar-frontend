package models

import play.api.libs.json.{Json, OFormat}

import java.util.UUID


case class UserPrivateWallet(
                              userWalletId:UUID,
                              userId:UUID,
                              name: String,
                              baseAddress:String,
                              privateKey:String,
                              //                       createdAt: ZonedDateTime,
                              walletSummery:Option[WalletSummary] = None
                            )

object UserPrivateWallet {
  implicit val f: OFormat[UserPrivateWallet] = Json.format[UserPrivateWallet]
}
