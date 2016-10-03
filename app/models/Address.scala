package models

import play.api.libs.json.Json

case class Address(street: String, town: String, zipCode: String)

object Address {

  implicit val addressFormat = Json.format[Address]

}