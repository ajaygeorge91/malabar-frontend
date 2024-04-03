package models

import play.api.libs.json.{Json, OFormat}

case class CreateWalletFromPublicKeyRequest(
                                             name: String,
                                             account_public_key: String,
                                             address_pool_gap: Int = 20
                                           )

object CreateWalletFromPublicKeyRequest {
  implicit val f: OFormat[CreateWalletFromPublicKeyRequest] = Json.format[CreateWalletFromPublicKeyRequest]
}
