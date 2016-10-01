package models

import org.joda.time.DateTime

/**
 * Created by fred on 17/04/2015.
 */

trait Sex
case object male extends Sex
case object female extends Sex

trait AddressType
case object personal extends Sex
case object professional extends Sex


case class Person(name:String, lastName:String, birthDate:DateTime, sex: Sex, addresses : Map[AddressType, Address] = Map.empty) {
  
  def age: Int = ???

  def professionalAddress: Option[Address] = ???

  def personalAddress: Option[Address]  = ???

}


object Person{
  //TODO hint : créer le Json Read et Write (ou Format)
}

object AddressType{
  //TODO hint : créer le Json Read et Write (ou Format)
}

object  Sex{
  //TODO hint : créer le Json Read et Write (ou Format)
}