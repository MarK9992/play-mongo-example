package controllers

import models.Person
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.MongoController
import reactivemongo.api.Cursor
import reactivemongo.bson._

import scala.concurrent.Future

trait PersonController extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("persons")

  /**
   * Lists all persons in database.
   *
   * @return  a JSON representation of all stored person resources in case of success (200),
   *          an internal server error (500) otherwise
   */
  def list = Action.async {
    val cursor: Cursor[Person] = collection.find(Json.obj()).cursor[Person]()
    val futureList: Future[List[Person]] = cursor.collect[List]()

    futureList.map { persons =>
      Ok(Json.toJson(persons))
    }.recover(error)
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
    val person = request.body

    collection.insert(person).map { writeResult =>
      Logger.debug(writeResult.toString)
      Created(Json.toJson(person))
    }.recover(error)
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
   * @return    not found status code if the given id doesn't match any element, the updated person's JSON otherwise
   */
  def update(id: String) = Action.async(parse.json[Person]) { request =>
    val updatedPersonJson = Json.toJson(request.body)
    val selector = BSONDocument("_id" -> BSONObjectID(id))
    val modifier = BSONDocument("$set" -> updatedPersonJson)

    collection.update(selector, modifier).map { writeResult =>
      Logger.debug(writeResult.toString)
      if (writeResult.n == 1) Ok(updatedPersonJson) else NotFound(id + " not found")
    }.recover(error)
  }

  /**
   * Removes a person matching the given id from the database.
   *
   * @param id  the person id to look for
   * @return    not found status code if the given id doesn't match any element, no content otherwise
   */
  def remove(id: String) = Action.async {
    val selector = BSONDocument("_id" -> BSONObjectID(id))

    collection.remove(selector, firstMatchOnly = true).map { writeResult =>
      Logger.debug(writeResult.toString)
      if (writeResult.n == 1) NoContent else NotFound(id + " not found")
    }.recover(error)
  }

  /**
   * Partial function handling error cases.
   *
   * @return  an appropriate Result
   */
  def error: PartialFunction[Throwable, Result] = {
    case t: Throwable =>
      Logger.error("Could not request MongoDB", t)
      InternalServerError("Could not request database.")
  }

}

object PersonController extends PersonController