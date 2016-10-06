package controllers

import models.{female, Person, male}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import services.{PersonStorage, StorageException}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class PersonControllerTest extends Specification with Mockito {
  
  final val PERSONS_IN_STORAGE = List(Person("Marc", "Karassev", new DateTime(1992, 9, 9, 1, 59), male),
                                      Person("Foo", "Bar", new DateTime(1980, 12, 25, 0, 0), male))

  final val BLAH = Person("Blah", "foobar", new DateTime(1970, 1, 1, 0, 0), female)
  
  final val BAD_PERSON_JSONS = Seq(
    """
      |{}
    """.stripMargin, """
      |{
      |  "name": "az",
      |  "lastName": "lastName",
      |  "sex": "male",
      |  "birthDate": "1990-01-01"
      |}
    """.stripMargin, """
      |{
      |  "name": "name",
      |  "lastName": "az",
      |  "sex": "male",
      |  "birthDate": "1990-01-01"
      |}
    """.stripMargin, """
      |{
      |  "name": "name",
      |  "lastName": "lastName",
      |  "sex": "male",
      |  "birthDate": "2000-01-01"
      |}
    """.stripMargin, """
      |{
      |  "name": "name",
      |  "lastName": "lastName",
      |  "sex": "male",
      |  "birthDate": "1900-01-01"
      |}
    """.stripMargin)

  "PersonController" should {

    "list persons" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.list() returns Future.successful(PERSONS_IN_STORAGE)
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val result = personController.list()(FakeRequest())
      status(result) must equalTo(OK)
      val content = contentAsJson(result).validate[List[Person]].get
      content must beEqualTo(PERSONS_IN_STORAGE)
    }

    "create a person" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.persist(any[Person]) answers( (person) => Future.successful(person.asInstanceOf[Person]) )
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(BLAH))
      val result = call(personController.create(), request)
      status(result) must equalTo(CREATED)
      val content = contentAsJson(result).validate[Person].get
      content must beEqualTo(BLAH)
    }

    "send bad request on bad person creation inputs" in {
      val personController = new PersonController { override protected def personStorage: PersonStorage = mock[PersonStorage] }

      BAD_PERSON_JSONS.foreach { input =>
        val request = FakeRequest().withJsonBody(Json.parse(input))
        val result = call(personController.create(), request)
        status(result) mustEqual BAD_REQUEST
      }
      ok
    }

    "update a person" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.replace(any[String], any[Person]) answers( (args, mock) =>
        Future.successful(Some(args.asInstanceOf[Array[Any]](1).asInstanceOf[Person]))
      )
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(BLAH))
      val result = call(personController.update("i"), request)
      status(result) must equalTo(OK)
      val content = contentAsJson(result).validate[Person].get
      content must beEqualTo(BLAH)
    }

    "send not found on person update if the given id does not exist" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.replace(any[String], any[Person]) returns Future.successful(None)
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val request = FakeRequest().withJsonBody(Json.toJson(BLAH))
      val result = call(personController.update("i"), request)
      status(result) must equalTo(NOT_FOUND)
    }

    "send bad request on bad person update inputs" in {
      val personController = new PersonController { override protected def personStorage: PersonStorage = mock[PersonStorage] }

      BAD_PERSON_JSONS.foreach { input =>
        val request = FakeRequest().withJsonBody(Json.parse(input))
        val result = call(personController.update("i"), request)
        status(result) mustEqual BAD_REQUEST
      }
      ok
    }

    "delete a person" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.remove(any[String]) returns Future.successful(Some(Unit))
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val result = call(personController.remove("i"), FakeRequest())
      status(result) must equalTo(NO_CONTENT)
    }

    "send not found on person delete if given id does not exist" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.remove(any[String]) returns Future.successful(None)
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val result = call(personController.remove("i"), FakeRequest())
      status(result) must equalTo(NOT_FOUND)
    }

    "send an internal error if storage fails" in {
      val personStorageMock = mock[PersonStorage]
      personStorageMock.list() returns Future.failed(StorageException("foo", null))
      personStorageMock.persist(any[Person]) returns Future.failed(StorageException("foo", null))
      personStorageMock.replace(any[String], any[Person]) returns Future.failed(StorageException("foo", null))
      personStorageMock.remove(any[String]) returns Future.failed(StorageException("foo", null))
      val personController = new PersonController { override protected def personStorage: PersonStorage = personStorageMock }

      val results: Seq[Future[Result]] = Seq(
        call(personController.list(), FakeRequest()),
        call(personController.create(), FakeRequest().withJsonBody(Json.toJson(BLAH))),
        call(personController.update("i"), FakeRequest().withJsonBody(Json.toJson(BLAH))),
        call(personController.remove("i"), FakeRequest())
      )
      results.foreach( (result: Future[Result]) =>
        status(result) must equalTo(INTERNAL_SERVER_ERROR)
      )
      ok
    }

  }

}
