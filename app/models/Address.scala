package models

import play.api.libs.json.{Format, Json}

case class Address(street: String, town: String, zipCode: String)

object Address {

  implicit val addressFormat: Format[Address] = Json.format[Address]

}