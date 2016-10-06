package services

import models.{AddressType, Person}

import scala.concurrent.Future

/**
 * Interface for person storage implementations.
 */
trait PersonStorage {

  /**
   * Lists asynchronously all persons in storage.
   *
   * @return  a future over a list of persons
   */
  def list(): Future[List[Person]]

  /**
   * Persists asynchronously a person.
   *
   * @param person  the person to persist
   * @return        a future over the persisted person
   */
  def persist(person: Person): Future[Person]

  /**
   * Removes asynchronously a person from storage.
   *
   * @param id  the person id to look for
   * @return    a future over an option with no content
   */
  def remove(id: String): Future[Option[Unit]]

  /**
   * Replaces asynchronously a person in storage.
   *
   * @param id      id of the person to replace
   * @param person  the new person
   * @return        a future over a newly persisted person option
   */
  def replace(id: String, person: Person): Future[Option[Person]]

  /**
   * Retrieves asynchronously a person from storage.
   *
   * @param id  the person id to look for
   * @return    a future over a person option
   */
  def retrieve(id: String): Future[Option[Person]]

  /**
   * Asynchronously removes an address to an existing person in storage.
   *
   * @param id          id of the person to remove the address
   * @param addressType the type of address to remove
   * @return            a future over the updated person
   */
  def removeAddress(id: String, addressType: AddressType): Future[Person]

}

/**
 * Storage exception.
 *
 * @param message exception message
 * @param cause   exception cause
 */
class StorageException(message: String, cause: Throwable) extends Exception(message, cause)

/**
 * Exception thrown on bad id inputs.
 *
 * @param id      the id responsible for the exception
 * @param cause   exception cause
 */
class BadIdException(id: String, cause: Throwable) extends StorageException("bad id: " + id, cause)
