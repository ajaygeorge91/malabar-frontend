package models

import iog.psg.cardano.CardanoApi.ErrorMessage
import play.api.libs.json.{Json, OFormat}

case class WalletErrorMessage(message:String) extends RuntimeException(message)

object WalletErrorMessage {
  implicit val f: OFormat[WalletErrorMessage] = Json.format[WalletErrorMessage]

  def fromCardanoErrorMessage(errorMessage:ErrorMessage):WalletErrorMessage = {
    WalletErrorMessage(errorMessage.code + "; " +errorMessage.message)
  }
}

