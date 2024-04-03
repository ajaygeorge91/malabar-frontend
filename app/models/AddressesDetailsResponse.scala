package models

import play.api.libs.json.{Json, OFormat}


case class Pointer (
                     slot_num: Int,
                     transaction_index: Int,
                     output_index: Int
                   )

case class AddressesDetailsResponse (
                           address_style: String,
                           stake_reference: String,
                           network_tag: Int,
                           spending_key_hash: Option[String],
                           spending_key_hash_bech32: Option[String],
                           stake_key_hash: Option[String],
                           stake_key_hash_bech32: Option[String],
                           script_hash: Option[String],
                           script_hash_bech32: Option[String],
                           pointer: Option[Pointer],
                           address_root: Option[String],
                           derivation_path: Option[String],
                           address_type: Int
                         )

object AddressesDetailsResponse {
  implicit val f: OFormat[AddressesDetailsResponse] = Json.format[AddressesDetailsResponse]
}

object Pointer {
  implicit val f: OFormat[Pointer] = Json.format[Pointer]
}
