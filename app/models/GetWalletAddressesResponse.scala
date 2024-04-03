package models

import play.api.libs.json.{Json, OFormat}


case class GetWalletAddressesResponse (
                                        id: String,
                                        state: String,
                                        derivation_path: Seq[String]
                                      )

object GetWalletAddressesResponse {
  implicit val f: OFormat[GetWalletAddressesResponse] = Json.format[GetWalletAddressesResponse]
}
