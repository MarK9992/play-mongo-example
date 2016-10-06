package services

import models.{Person, Address, AddressType}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api.Cursor

import scala.concurrent.Future

/**
 * Person storage using MongoDB through ReactiveMongo.
 */
class MongoPersonStorage extends PersonStorage {

  private val db = ReactiveMongoPlugin.db
  private val personsCollection = db.collection(MongoPersonStorage.PERSONS_COLLECTION_NAME)

  /**
   * @inheritdoc
   */
  override def list(): Future[List[Person]] = {
    val cursor: Cursor[Person] = personsCollection.find(Json.obj()).cursor[Person]()
    val futureList: Future[List[Person]] = cursor.collect[List]()

    futureList.recover(recover)
  }

  /**
   * @inheritdoc
   */
  override def replace(id: String, person: Person): Future[Person] = ???

  /**
   * @inheritdoc
   */
  override def persist(person: Person): Future[Person] = {
    personsCollection.insert(person).map { writeResult =>
      Logger.debug(writeResult.toString)
      person
    }.recover(recover)
  }

  /**
   * @inheritdoc
   */
  override def remove(id: String): Future[Unit] = ???

  /**
   * @inheritdoc
   */
  override def retrieve(id: String): Future[Option[Person]] = ???

  /**
   * @inheritdoc
   */
  override def replaceAddress(id: String, addressType: AddressType, address: Address): Future[Person] = ???

  /**
   * @inheritdoc
   */
  override def removeAddress(id: String, addressType: AddressType): Future[Person] = ???

  /**
   * @inheritdoc
   */
  override def addAddress(id: String, addressType: AddressType, address: Address): Future[Person] = ???

  /* Logs and encapsulates a Throwable into a StorageException. */
  private def recover[T]: PartialFunction[Throwable, T] = {
    case t: Throwable =>
      Logger.error("Could not request MongoDB", t)
      throw new StorageException("mongodb error", t)
  }

}

object MongoPersonStorage {

  private final val PERSONS_COLLECTION_NAME = "persons"

  def apply(): MongoPersonStorage = new MongoPersonStorage()

}