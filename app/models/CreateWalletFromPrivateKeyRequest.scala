package models

import play.api.libs.json.{Json, OFormat}

case class CreateWalletFromPrivateKeyRequest(
                                              name: String,
                                              mnemonic_sentence: Seq[String],
                                              passphrase: String,
                                              mnemonic_second_factor: Option[Seq[String]] = None,
                                              address_pool_gap: Int = 20
                                            )

object CreateWalletFromPrivateKeyRequest {
  implicit val f: OFormat[CreateWalletFromPrivateKeyRequest] = Json.format[CreateWalletFromPrivateKeyRequest]
}

