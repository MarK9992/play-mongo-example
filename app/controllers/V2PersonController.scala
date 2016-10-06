package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.Action
import services.{MongoPersonStorage, PersonStorage}

/**
 * Outputs persons with a different format.
 */
trait V2PersonController extends PersonStorageController {

  def list = Action.async {
    personStorage.list().map { persons =>
      val transformedJsons = persons.map { person =>
        val json = Json.toJson(person)
        val transformed = json.transform(
          __.json.update( (__ \ 'addresses).json.pick ) andThen
          (__ \ 'addresses).json.prune
        )
        Json.toJsFieldJsValueWrapper(transformed.get)
      }
      Ok(Json.arr(transformedJsons.toSeq:_*))
    }.recover(recover)
  }

}

object V2PersonController extends V2PersonController {

  override protected def personStorage: PersonStorage = MongoPersonStorage()

}
