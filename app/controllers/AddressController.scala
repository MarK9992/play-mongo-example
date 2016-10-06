package controllers

import models.{Address, AddressType, Person}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.{StorageException, MongoPersonStorage, PersonStorage}

import scala.concurrent.Future

trait AddressController extends Controller {

  def personStorage: PersonStorage

  /**
   * Adds an address to an existing person in storage. Request body should be a JSON with the following properties:
   *  "street" (string, min 1 char),
   *  "town" (string, min 1 char) and
   *  "zipCode" (string, min 1 char).
   *
   * @param personId  the person's id in database
   * @param kind      the address type to add
   *
   * @return          the updated person's JSON on success,
   *                  bad request status code if the request body is not in the appropriate format or if the matching
   *                  person already has an address of the given type,
   *                  not found if the given address type does not exist or if the given id did not match any person in
   *                  database
   */
  def add(personId: String, kind: String) = Action.async(parse.json[Address]) { request =>
    AddressType.matchString(kind) match {
      case None               =>  Future.successful(NotFound("no " + kind + "address type"))
      case Some(addressType)  =>
        personStorage.retrieve(personId).flatMap {
          case None =>
            Future.successful(NotFound("person id " + personId + " not found"))
          case Some(person) if person.addresses contains addressType  =>  // No handling of concurrent accesses.
            Future.successful(BadRequest(s"person $personId already has a $addressType address"))
          case Some(person) =>
            setAddress(personId, person, addressType, request.body, Created.apply)
        }.recover(recover)
    }
  }

  /**
   * Updates an addres to an existing person in storage. Request body should be a JSON with the following properties:
   *  "street" (string, min 1 char),
   *  "town" (string, min 1 char) and
   *  "zipCode" (string, min 1 char).
   *
   * @param personId  the person's id in database
   * @param kind      the address type to add
   *
   * @return          the updated person's JSON on success,
   *                  bad request status code if the request body is not in the appropriate format,
   *                  not found if the given address type does not exist, if the given id did not match any person in
   *                  database or if the matching person does not already have an address of given type
   */
  def update(personId: String, kind: String) = Action.async(parse.json[Address]) { request =>
    AddressType.matchString(kind) match {
      case None               =>  Future.successful(NotFound("no " + kind + "address type"))
      case Some(addressType)  =>
        personStorage.retrieve(personId).flatMap {
          case None         =>
            Future.successful(NotFound("person id " + personId + " not found"))
          case Some(person) if !(person.addresses contains addressType) =>
            Future.successful(NotFound(s"person $personId does not have a $addressType address"))
          case Some(person) =>
            setAddress(personId, person, addressType, request.body, Ok.apply)
        }.recover(recover)
    }
  }

  /**
   *
   * @param personId : l'id de la personne
   * @param typeAdress : le type d'adresse (perso, pro)
   *
   * @return : le Json représentant la personne créée
   *
   * hint : la sérialisation de l'id est la représentation string du BsonObjectId
   * hint : renvoyer un 404 si id inexistant ou si cette personne n'a pas d'adresse de ce type
   *
   */
  def remove(personId:String, typeAdress:String) = Action.async {
  //TODO Attention aux hints
    Future.successful(Ok(Json.obj()))
  }

  /* Performs the call to set an address. */
  private def setAddress(personId: String, person: Person, addressType: AddressType, address: Address, result: (JsValue) => Result): Future[Result] = {
    val newPerson = Person(person.name, person.lastName, person.birthDate, person.sex, person.addresses + (addressType -> address))

    personStorage.replace(personId, newPerson).map {
      case None                 =>  NotFound("person id " + personId + " not found")
      case Some(updatedPerson)  =>  result(Json.toJson(updatedPerson))
    }.recover(recover)
  }

  /**
   * Partial function handling error cases.
   *
   * @return  an appropriate Result
   */
  def recover: PartialFunction[Throwable, Result] = {
    case se: StorageException =>
      Logger.error("Could not request database", se)
      InternalServerError("Could not request database.")
  }

}

object AddressController extends AddressController {

  override def personStorage: PersonStorage = MongoPersonStorage()

}