package models

import play.api.libs.json.{Json, OFormat}

import java.time.ZonedDateTime
import java.util.UUID


case class UserPublicWallet(
                       userWalletId:UUID,
                       userId:UUID,
                       name: String,
                       walletId:String,
                       publicKey:String,
                       //                       createdAt: ZonedDateTime,
                       walletSummery:Option[WalletSummary] = None
                     )

object UserPublicWallet {
  implicit val f: OFormat[UserPublicWallet] = Json.format[UserPublicWallet]
}
