package models

import play.api.libs.json.{Json, OFormat}


case class CardanoErrorResponse(
                                 message: String,
                                 code: String
                               ) extends RuntimeException

object CardanoErrorResponse {
  implicit val f: OFormat[CardanoErrorResponse] = Json.format[CardanoErrorResponse]
}
