package models

import play.api.libs.json.{Json, OFormat}


case class CreateNftAsset(description: String,
                          name:String,
                          image:String,
                          author:String)
//                       authors:Option[Seq[Author]])

object CreateNftAsset {
  implicit val f: OFormat[CreateNftAsset] = Json.format[CreateNftAsset]
}

case class Author(name:String,
                  socialProfiles:Option[Seq[SocialProfile]])

object Author {
  implicit val f: OFormat[Author] = Json.format[Author]
}

case class SocialProfile(domain:String,
                         userName:String,
                         id:Option[String])

object SocialProfile {
  implicit val f: OFormat[SocialProfile] = Json.format[SocialProfile]
}
