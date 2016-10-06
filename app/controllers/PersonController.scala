package controllers

import models.Person
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.{MongoPersonStorage, PersonStorage}

trait PersonController extends PersonStorageController {

  /**
   * Lists all persons in database.
   *
   * @return  a JSON representation of all stored person resources in case of success (200),
   *          an internal server error (500) otherwise
   */
  def list = Action.async {
    personStorage.list().map { persons =>
      Ok(Json.toJson(persons))
    }.recover(recover)
  }

  /**
   * Creates and persists an Person. The request body must consist of a JSON with the following properties set:
   *  "name" (string, min 3 chars),
   *  "lastName" (string, min 3 chars),
   *  "birthDate" (string, yy-mm-dd, age has to be between 18 and 100),
   *  "sex" ("male" or "female") and
   *  "addresses" (optional, object with "personal" and "professional" properties set to object values with "street",
   *    "town", "zipCode" string valued properties)
   *
   * @return  bad request status code if the given data does not match the previous format, the created person's JSON
   *          otherwise
   */
  def create = Action.async(parse.json[Person]) { request =>
    personStorage.persist(request.body).map { person =>
      Created(Json.toJson(person))
    }.recover(recover)
  }

  /**
   * Updates a persisted Person matching the given id. The request body must consist of a JSON with the following
   * properties set:
   *  "name" (string, min 3 chars),
   *  "lastName" (string, min 3 chars),
   *  "birthDate" (string, yy-mm-dd, age has to be between 18 and 100),
   *  "sex" ("male" or "female") and
   *  "addresses" (optional, object with "personal" and "professional" properties set to object values with "street",
   *    "town", "zipCode" string valued properties)
   *
   * @param id  the id to look for
   * @return    not found status code if the given id doesn't match any element, bad request status code if the request
   *            body is not in the appropriate format, the updated person's JSON otherwise
   */
  def update(id: String) = Action.async(parse.json[Person]) { request =>
    personStorage.replace(id, request.body).map {
      case Some(person) =>  Ok(Json.toJson(person))
      case None         =>  NotFound(id + " not found")
    }.recover(recover)
  }

  /**
   * Removes a person matching the given id from the database.
   *
   * @param id  the person id to look for
   * @return    not found status code if the given id doesn't match any element, no content otherwise
   */
  def remove(id: String) = Action.async {
    personStorage.remove(id).map {
      case Some(nothing)  =>  NoContent
      case None           =>  NotFound(id + " not found")
    }.recover(recover)
  }

}

object PersonController extends PersonController {

  override protected def personStorage: PersonStorage = MongoPersonStorage()

}