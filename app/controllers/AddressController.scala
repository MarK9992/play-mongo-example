package controllers

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

object AddressController extends Controller {

  /**
   * @param personId : l'id de la personne
   * @param typeAdress : le type d'adresse (perso, pro)
   *
   * @return : le Json représentant la personne créée
   *
   * body contient un json représentant l'adresse
   *
   * validation : renvoyer un 404 si id inéxistant
   * validation : renvoyer un badRequest si le type d'adresse existe deja
   * validation : renvoyer un badRequest si un des champs obligatoire est vide ou ""
   *
   */
  def add(personId:String, typeAdress:String) = Action.async {
    //TODO implementer l'ajout d'une adresse à une personne à partir d'un json contenant les informations adresse
    // Attention aux hints
    Future.successful(Ok(Json.obj()))
  }

  /**
   * @param personId : l'id de la personne
   * @param typeAdress : le type d'adresse (perso, pro)
   *
   * body contient un json représantant l'adresse
   *
   * @return : le Json représentant la personne créée
   *
   * validation : renvoyer un 404 si id ou type inexistant
   * validation : renvoyer un badRequest si un des champs obligatoire est vide ou ""
   *
   */
  def update(personId:String,typeAdress:String ) = Action.async {
    //TODO implementer la modification d'une adresse d'une personne à partir d'un json contenant les informations adresse
    // Attention aux hints
    Future.successful(Ok(Json.obj()))
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

}