package models

import org.joda.time.{DateTime, Years}
import play.api.libs.json._

sealed trait Sex
case object male extends Sex
case object female extends Sex

sealed trait AddressType
case object personal extends AddressType
case object professional extends AddressType

case class Person(name: String, lastName: String, birthDate: DateTime, sex: Sex, addresses: Map[AddressType, Address] = Map.empty) {
  
  def age: Int = Years.yearsBetween(birthDate, DateTime.now(birthDate.getZone)).getYears

  def professionalAddress: Option[Address] = addresses get professional

  def personalAddress: Option[Address]  = addresses get personal

}

object Person {

  import play.api.libs.functional.syntax._

  import scala.collection.mutable

  private implicit val addressesReads: Reads[Map[AddressType, Address]] = (
    (JsPath \ personal.toString).readNullable[Address] and
    (JsPath \ professional.toString).readNullable[Address]
  )( (personalAddress, professionalAddress) => {
      val addresses: mutable.Map[AddressType, Address] = mutable.Map.empty
      if (personalAddress.isDefined) addresses += (personal -> personalAddress.get)
      if (professionalAddress.isDefined) addresses += (professional -> professionalAddress.get)
      addresses.toMap
    }
  )

  private implicit val addressesWrites = (
    (JsPath \ personal.toString).writeNullable[Address] and
    (JsPath \ professional.toString).writeNullable[Address]
  )( (map: Map[AddressType, Address]) => (map get personal, map get professional) )

  implicit val personFormat = Json.format[Person]

}

object AddressType {

  /** Could not use JSON inception because I don't want an AddressType value to be represented as a JSON
    * object but rather as a JSON string value. */
  implicit val addressTypeFormat: Format[AddressType] = new Format[AddressType] {

    override def reads(json: JsValue): JsResult[AddressType] = json match {
      case JsString("personal")      => JsSuccess(personal)
      case JsString("professional")  => JsSuccess(professional)
      case _                         => JsError("could not parse: " + json)
    }

    override def writes(o: AddressType): JsValue = JsString(o.toString)

  }

}

object Sex {

  /** Could not use JSON inception because I don't want a Sex value to be represented as a JSON object but rather as
    * a JSON string value. */
  implicit val sexFormat: Format[Sex] = new Format[Sex] {

    override def reads(json: JsValue): JsResult[Sex] = json match {
      case JsString("male")    => JsSuccess(male)
      case JsString("female")  => JsSuccess(female)
      case _                   => JsError("could not parse: " + json)
    }

    override def writes(o: Sex): JsValue = JsString(o.toString)

  }

}