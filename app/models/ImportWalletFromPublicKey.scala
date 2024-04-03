package models

import play.api.libs.json.{Json, OFormat}


case class ImportWalletFromPublicKey(walletName: String, publicKey:String)

object ImportWalletFromPublicKey {
  implicit val f: OFormat[ImportWalletFromPublicKey] = Json.format[ImportWalletFromPublicKey]
}
