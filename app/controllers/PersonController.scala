package controllers

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

object PersonController extends Controller {

  /**
   * @return : le Json represantant la liste des personnes dans mongo
   *
   */
  def list = Action.async {
  //TODO
    Future.successful(Ok(Json.obj()))
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