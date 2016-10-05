package controllers

import models.Person
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import reactivemongo.api.Cursor

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
   * Creates and persists an Person from a JSON in the following properties set:
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
      Ok(Json.toJson(person))
    }.recover(error)
  }

  def update(id: String) = Action.async {
    //TODO
    Future.successful(Ok(Json.obj()))
  }

  /**
   *
   * @param id
   * @return : le Json represantant la personne créée
   *
   * TODO implementer la suppression d'une personne à partir de son id sérialisé
   *
   * hint : la sérialisation de l'id est la représentation string du BsonObjectId
   * hint : renvoyer un 404 si id inexistant
   *
   */
  def remove(id:String) = Action.async {
  //TODO
    Future.successful(Ok(Json.obj()))
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