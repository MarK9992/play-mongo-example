package models

import org.joda.time.{Years, DateTime}

/**
 * @author Marc Karassev
 */

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
  //TODO hint : créer le Json Read et Write (ou Format)
}

object AddressType {
  //TODO hint : créer le Json Read et Write (ou Format)
}

object Sex {
  //TODO hint : créer le Json Read et Write (ou Format)
}