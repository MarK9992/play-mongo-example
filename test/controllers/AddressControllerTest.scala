package controllers

import models._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import services.{StorageException, PersonStorage}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class AddressControllerTest extends Specification with Mockito {

  final val PERSONAL_ADDRESS = Address("personal", "potato", "777")
  final val PROFESSIONAL_ADDRESS = Address("professional", "potato", "777")
  final val BLAH = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female)
  final val BLAH_WITH_PERSONAL_ADDRESS = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female, Map(personal -> PERSONAL_ADDRESS))
  final val BLAH_WITH_PROFESSIONAL_ADDRESS = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female, Map(professional -> PROFESSIONAL_ADDRESS))
  final val BLAH_WITH_PROFESSIONAL_ADDRESS_INSTEAD_OF_PERSONAL = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female, Map(personal -> PROFESSIONAL_ADDRESS))
  final val BLAH_ID = "i"

  final val BAD_ADDRESS_JSONS = Seq(
    """
      |{}
    """.stripMargin, """
      |{
      |  "street": "12 rue de test",
      |  "town": "testville",
      |  "zipCode": ""
      |}
    """.stripMargin, """
      |{
      |  "streeet": "12 rue de test",
      |  "town": "testville",
      |  "zipCode": "1234"
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
      
      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val result = call(addressController.add(BLAH_ID, personal.toString), request)
      status(result) must equalTo(CREATED)
      val content = contentAsJson(result).validate[Person].get
      content must beEqualTo(BLAH_WITH_PERSONAL_ADDRESS)
    }

    "add a professional address" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(Some(BLAH))
      personStorageMock.replace(any[String], any[Person]) answers( (args, mock) =>
        Future.successful(Some(args.asInstanceOf[Array[Any]](1).asInstanceOf[Person]))
        )
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PROFESSIONAL_ADDRESS))
      val result = call(addressController.add(BLAH_ID, professional.toString), request)
      status(result) must equalTo(CREATED)
      val content = contentAsJson(result).validate[Person].get
      content must beEqualTo(BLAH_WITH_PROFESSIONAL_ADDRESS)
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

    "send bad request on address add if person already has given address type" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(Some(BLAH_WITH_PROFESSIONAL_ADDRESS))
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PROFESSIONAL_ADDRESS))
      val result = call(addressController.add(BLAH_ID, professional.toString), request)
      status(result) must equalTo(BAD_REQUEST)
    }

    "send not found on address add if given id does not exist" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(None)
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val result = call(addressController.add(BLAH_ID, personal.toString), request)
      status(result) mustEqual NOT_FOUND
    }

    "send not found on address add if given address type does not exist" in {
      val addressController = new AddressController { override def personStorage: PersonStorage = mock[PersonStorage] }

      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val result = call(addressController.add(BLAH_ID, "foo"), request)
      status(result) mustEqual NOT_FOUND
    }
    
    "update an address" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(Some(BLAH_WITH_PERSONAL_ADDRESS))
      personStorageMock.replace(any[String], any[Person]) answers( (args, mock) =>
        Future.successful(Some(args.asInstanceOf[Array[Any]](1).asInstanceOf[Person]))
        )
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PROFESSIONAL_ADDRESS))
      val result = call(addressController.update(BLAH_ID, personal.toString), request)
      status(result) must equalTo(OK)
      val content = contentAsJson(result).validate[Person].get
      content must beEqualTo(BLAH_WITH_PROFESSIONAL_ADDRESS_INSTEAD_OF_PERSONAL)
    }

    "send bad request on bad address update inputs" in {
      val addressController = new AddressController { override def personStorage: PersonStorage = mock[PersonStorage] }

      BAD_ADDRESS_JSONS.foreach { input =>
        val request = FakeRequest().withJsonBody(Json.parse(input))
        val result = call(addressController.update(BLAH_ID, personal.toString), request)
        status(result) mustEqual BAD_REQUEST
      }
      ok
    }

    "send not found on address update if person does not have given address type" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(Some(BLAH))
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PROFESSIONAL_ADDRESS))
      val result = call(addressController.update(BLAH_ID, professional.toString), request)
      status(result) must equalTo(NOT_FOUND)
    }

    "send not found on address update if given id does not exist" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.retrieve(BLAH_ID) returns Future.successful(None)
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val result = call(addressController.update(BLAH_ID, personal.toString), request)
      status(result) mustEqual NOT_FOUND
    }

    "send not found on address update if given address type does not exist" in {
      val addressController = new AddressController { override def personStorage: PersonStorage = mock[PersonStorage] }

      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val result = call(addressController.update(BLAH_ID, "foo"), request)
      status(result) mustEqual NOT_FOUND
    }

    "send an internal error if storage fails" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.replace(any[String], any[Person]) returns Future.failed(new StorageException("foo", null))
      personStorageMock.retrieve(any[String]) returns Future.failed(new StorageException("foo", null))
      val addressController = new AddressController { override def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(PERSONAL_ADDRESS))
      val results: Seq[Future[Result]] = Seq(
        call(addressController.add(BLAH_ID, personal.toString), request),
        call(addressController.update(BLAH_ID, personal.toString), request)//,
//        call(addressController.remove(BLAH_ID, personal.toString), FakeRequest())
      )
      results.foreach( (result: Future[Result]) =>
        status(result) must equalTo(INTERNAL_SERVER_ERROR)
      )
      ok
    }

  }

}
