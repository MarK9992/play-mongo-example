package models

import org.joda.time.DateTime
import play.api.libs.json._

sealed trait Sex
case object male extends Sex
case object female extends Sex

sealed trait AddressType
case object personal extends AddressType
case object professional extends AddressType

case class Person(name: String, lastName: String, birthDate: DateTime, sex: Sex, addresses: Map[AddressType, Address] = Map.empty) {
  
  def age: Int = Person.age(birthDate)

  def professionalAddress: Option[Address] = addresses get professional

  def personalAddress: Option[Address]  = addresses get personal

}

object Person {

  import org.joda.time.Years
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  import scala.collection.mutable

  /* Computes the time elapsed in years between a given date and now. */
  private def age(birthDate: DateTime): Int = Years.yearsBetween(birthDate, DateTime.now(birthDate.getZone)).getYears

  // Addresses map JSON Reads.
  private implicit val addressesReads: Reads[Map[AddressType, Address]] = (
    (JsPath \ "personal").readNullable[Address] and
    (JsPath \ "professional").readNullable[Address]
  )( (personalAddress, professionalAddress) => {
      val addresses: mutable.Map[AddressType, Address] = mutable.Map.empty
      if (personalAddress.isDefined) addresses += (personal -> personalAddress.get)
      if (professionalAddress.isDefined) addresses += (professional -> professionalAddress.get)
      addresses.toMap
    }
  )

  // Addresses map JSON Writes.
  private implicit val addressesWrites = (
    (JsPath \ "personal").writeNullable[Address] and
    (JsPath \ "professional").writeNullable[Address]
  )( (map: Map[AddressType, Address]) => (map get personal, map get professional) )

  // Person JSON Reads, validates age and names length.
  implicit val personReads: Reads[Person] = (
    (JsPath \ "name").read[String](minLength[String](3)) and
    (JsPath \ "lastName").read[String](minLength[String](3)) and
    (JsPath \ "birthDate").read[DateTime](verifying((birth: DateTime) => 18 until 100 contains age(birth))) and
    (JsPath \ "sex").read[Sex] and
    (JsPath \ "addresses").readNullable[Map[AddressType, Address]]
  )( (name, lastName, birthDate, sex, addresses) => Person(name, lastName, birthDate, sex, addresses.getOrElse(Map.empty)))

  // Person JSON Writes.
  /* I did not understand why play/reactive-mongo forced me to define an OWrites instead of a Writes when I wasn't using
  JSON inception with Json.format[Person]... */
  implicit val personOWrites: OWrites[Person] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "birthDate").write[DateTime] and
    (JsPath \ "sex").write[Sex] and
    (JsPath \ "addresses").write[Map[AddressType, Address]]
  )(unlift(Person.unapply))

}

object AddressType {

  def apply(str: String): AddressType = matchString(str) match {
    case Some(value)  =>  value
    case None         =>  throw new IllegalArgumentException("address type " + str + "does not exist")
  }

  /* Matches a given String to an AddressType. */
  def matchString(str: String): Option[AddressType] = str match {
    case "personal"       =>  Some(personal)
    case "professional"   =>  Some(professional)
    case _                =>  None
  }

  // AddressType JSON Format.
  /* Could not use JSON inception because I don't want an AddressType value to be represented as a JSON object but
  rather as a JSON string value. */
  implicit val addressTypeFormat: Format[AddressType] = new Format[AddressType] {

    override def reads(json: JsValue): JsResult[AddressType] = json match {
      case JsString(str)  =>  AddressType.matchString(str) match {
        case Some(value)      =>  JsSuccess(value)
        case None             =>  JsError("could not parse as AddressType: " + str)
      }
      case _              =>  JsError("could not parse as String: " + json)
    }

    override def writes(o: AddressType): JsValue = JsString(o.toString)

  }

}

object Sex {

  // Sex JSON Format.
  /* Could not use JSON inception because I don't want a Sex value to be represented as a JSON object but rather as a
  JSON string value. */
  implicit val sexFormat: Format[Sex] = new Format[Sex] {

    override def reads(json: JsValue): JsResult[Sex] = json match {
      case JsString("male")    => JsSuccess(male)
      case JsString("female")  => JsSuccess(female)
      case _                   => JsError("could not parse: " + json)
    }

    override def writes(o: Sex): JsValue = JsString(o.toString)

  }

}