package controllers

import play.api.Logger
import play.api.mvc.{Controller, Result}
import services.{StorageException, BadIdException, PersonStorage}

/**
 * Abstract controller using the person storage.
 */
trait PersonStorageController extends Controller {

  protected def personStorage: PersonStorage

  /**
   * Partial function handling error cases.
   *
   * @return  an appropriate Result
   */
  protected def recover: PartialFunction[Throwable, Result] = {
    case bide: BadIdException =>
      val message = bide.getMessage
      NotFound("id" + message.substring(message.indexOf(":")) + " does not exist")
    case se: StorageException =>
      Logger.error("Could not request database", se)
      InternalServerError("Could not request database.")
  }

}
