package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Address(street: String, town: String, zipCode: String)

object Address {

  implicit val addressReads: Reads[Address] = (
    (__ \ "street").read[String](minLength[String](1)) and
    (__ \ "town").read[String](minLength[String](1)) and
    (__ \ "zipCode").read[String](minLength[String](1))
  )(Address.apply _)

  implicit val addressWrites: Writes[Address] = Json.writes[Address]

}