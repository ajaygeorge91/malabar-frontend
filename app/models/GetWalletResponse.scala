package models

import play.api.libs.json.{Json, OFormat}
import utils.Utils


case class GetWalletResponse (
                               id: String,
                               address_pool_gap: Int,
                               balance: Balance,
                               assets: AvailableAndTotalAssets,
                               delegation: Delegation,
                               name: String,
                               state: State,
                               tip: Tip,
                               passphrase: Option[Passphrase]
                             )

object GetWalletResponse {
  implicit val f: OFormat[GetWalletResponse] = Json.format[GetWalletResponse]
}


case class Active (
                    status: String,
                    target: Option[String]
                  )

object Active {
  implicit val f: OFormat[Active] = Json.format[Active]
}

case class AvailableAndTotalAssets(
                                    available: Seq[Assets],
                                    total: Seq[Assets]
                                  )

object AvailableAndTotalAssets {
  implicit val f: OFormat[AvailableAndTotalAssets] = Json.format[AvailableAndTotalAssets]
}

case class QuantityAndUnit (
                             quantity: Long,
                             unit: String
                           )

object QuantityAndUnit {
  implicit val f: OFormat[QuantityAndUnit] = Json.format[QuantityAndUnit]
}

case class Assets(
                   policy_id: String,
                   asset_name: String,
                   quantity: Long
                 ){

  def name: String = Utils.hexToString(asset_name)
}

object Assets {
  implicit val f: OFormat[Assets] = Json.format[Assets]
}

case class Balance (
                     available: QuantityAndUnit,
                     reward: QuantityAndUnit,
                     total: QuantityAndUnit
                   )

object Balance {
  implicit val f: OFormat[Balance] = Json.format[Balance]
}

case class ChangesAt (
                       epoch_number: Long,
                       epoch_start_time: String
                     )

object ChangesAt {
  implicit val f: OFormat[ChangesAt] = Json.format[ChangesAt]
}

case class Delegation (
                        active: Active,
                        next: Seq[Next]
                      )

object Delegation {
  implicit val f: OFormat[Delegation] = Json.format[Delegation]
}

case class Next (
                  status: String,
                  changes_at: ChangesAt,
                  target: String
                )

object Next {
  implicit val f: OFormat[Next] = Json.format[Next]
}

case class Passphrase (
                        last_updated_at: String
                      )

object Passphrase {
  implicit val f: OFormat[Passphrase] = Json.format[Passphrase]
}

case class State (
                   status: String
                 )

object State {
  implicit val f: OFormat[State] = Json.format[State]
}

case class Tip (
                 absolute_slot_number: Long,
                 slot_number: Long,
                 epoch_number: Long,
                 time: String,
                 height: QuantityAndUnit
               )

object Tip {
  implicit val f: OFormat[Tip] = Json.format[Tip]
}


