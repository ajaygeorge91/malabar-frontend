package models

import play.api.libs.json.{JsValue, Json, OFormat}


case class GetWalletTransactionsResponse(
                                          id: String,
                                          amount: QuantityAndUnit,
                                          fee: QuantityAndUnit,
                                          deposit: QuantityAndUnit,
                                          direction: String,
                                          inputs: Seq[Inputs],
                                          outputs: Seq[Outputs],
                                          withdrawals: Seq[Withdrawals],
                                          mint: Seq[Mint],
                                          status: String,
                                          inserted_at: InsertedAt,
                                          expires_at: Option[ExpiresAt],
                                          pending_since: Option[InsertedAt],
                                          depth: QuantityAndUnit,
                                          collateral: Seq[Collateral],
                                          metadata: Option[JsValue],
                                          script_validity: Option[String]
                                        )


case class Withdrawals (
                         stake_address: String,
                         amount: QuantityAndUnit
                       )


case class Collateral (
                        id: String,
                        index: Int,
                        address: String,
                        amount: QuantityAndUnit
                      )

case class ExpiresAt (
                       absolute_slot_number: Int,
                       slot_number: Int,
                       epoch_number: Int,
                       time: String
                     )

case class Inputs (
                    id: String,
                    index: Int,
                    address: Option[String],
                    amount: Option[QuantityAndUnit],
                    assets: Option[Seq[Assets]]
                  )

case class InsertedAt (
                        absolute_slot_number: Int,
                        slot_number: Int,
                        epoch_number: Int,
                        time: String,
                        height: QuantityAndUnit
                      )


case class Mint (
                  policy_id: String,
                  asset_name: String,
                  quantity: Int,
                  fingerprint: String
                )

case class Outputs (
                     address: String,
                     amount: QuantityAndUnit,
                     assets: Seq[Assets]
                   )

object GetWalletTransactionsResponse {
  implicit val f: OFormat[GetWalletTransactionsResponse] = Json.format[GetWalletTransactionsResponse]
}

object Withdrawals {
  implicit val f: OFormat[Withdrawals] = Json.format[Withdrawals]
}

object Collateral {
  implicit val f: OFormat[Collateral] = Json.format[Collateral]
}

object ExpiresAt {
  implicit val f: OFormat[ExpiresAt] = Json.format[ExpiresAt]
}

object Inputs {
  implicit val f: OFormat[Inputs] = Json.format[Inputs]
}

object InsertedAt {
  implicit val f: OFormat[InsertedAt] = Json.format[InsertedAt]
}

object Mint {
  implicit val f: OFormat[Mint] = Json.format[Mint]
}

object Outputs {
  implicit val f: OFormat[Outputs] = Json.format[Outputs]
}




