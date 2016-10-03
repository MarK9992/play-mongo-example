package controllers

import models.{Person, male}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Cursor

import scala.concurrent.Future

object PersonController extends Controller with MongoController {

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
    }.recover {
      case t: Throwable =>
        Logger.error("Could not request MongoDB", t)
        InternalServerError("Could not request database.")
    }
  }

  /**
   * @return : le Json represantant la personne créée
   *
   * TODO implementer la creation d'une personne à partir d'un json contenant les informations (name, lastName, birthDate, sex)
   *      cf classe Person
   * validation :
   *    renvoie un badRequest si nom ou prénom font moins de 3 caractères alpha
   *    renvoie un badRequest si age n'est pas 18 <= age < 100
   *
   */
  def create = Action.async {
    val person = Person("Marc", "Karassev", new DateTime(1992, 9, 9, 1, 59, DateTimeZone.UTC), male)
    collection.insert(person).map(lastError => Ok("Mongo LastError: %s".format(lastError)))
  //TODO
//    Future.successful(Ok(Json.obj()))
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

}