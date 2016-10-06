package controllers

import models.{personal, Address, female, Person}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.PersonStorage

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class AddressControllerTest extends Specification with Mockito {

  final val ADDRESS = Address("tomato", "potato", "777")
  final val BLAH = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female)
  final val BLAH_ID = "i"

  final val BAD_ADDRESS_JSONS = Seq(
    """
      |{}
    """.stripMargin, """
      |{
      |  "streeet": "",
      |  "town": "",
      |  "zipCode": ""
      |}
    """.stripMargin)

  "AddressController" should {

    "add a personal address" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(Some(BLAH))
      personStorageMock.replace(any[String], any[Person]) answers( (args, mock) =>
        Future.successful(Some(args.asInstanceOf[Array[Any]](1).asInstanceOf[Person]))
      )
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }
      
      val request = FakeRequest().withJsonBody(Json.toJson(ADDRESS))
      val result = call(addressController.add(BLAH_ID, personal.toString), request)
      status(result) must equalTo(CREATED)
      val content = contentAsJson(result).validate[Person].get
      content.addresses must beEqualTo(Map( personal -> ADDRESS))
    }

    "send bad request on bad address add inputs" in {
      val addressController = new AddressController { override def personStorage: PersonStorage = mock[PersonStorage] }

      BAD_ADDRESS_JSONS.foreach { input =>
        val request = FakeRequest().withJsonBody(Json.parse(input))
        val result = call(addressController.add(BLAH_ID, personal.toString), request)
        status(result) mustEqual BAD_REQUEST
      }
      ok
    }

    "send not found on address add if given id does not exist" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(None)
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(ADDRESS))
      val result = call(addressController.add(BLAH_ID, personal.toString), request)
      status(result) mustEqual NOT_FOUND
    }

    "send not found on address add if given address type does not exist" in {
      val addressController = new AddressController { override def personStorage: PersonStorage = mock[PersonStorage] }

      val request = FakeRequest().withJsonBody(Json.toJson(ADDRESS))
      val result = call(addressController.add(BLAH_ID, "foo"), request)
      status(result) mustEqual NOT_FOUND
    }

  }

}
