package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign up process.
 */
object SubmitCreateAssetForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "name" -> optional(text),
      "description" -> optional(text),
      "image" -> optional(text),
      "author" -> optional(text),
      "addresses" -> optional(text),
      "transactionToSign" -> nonEmptyText,
      "witnessSet" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
   * @param email The email of the user.
   * @param password The password of the user.
   */
  case class Data(
                  name: Option[String],
                  description: Option[String],
                  image:Option[String],
                  author:Option[String],
                  addresses:Option[String],
                  transactionToSign:String,
                  witnessSet:String
                 )
}
