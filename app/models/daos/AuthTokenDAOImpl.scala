package models.daos

import java.util.{TimeZone, UUID}
import models.AuthToken
import models.daos.AuthTokenDAOImpl._
import org.joda.time.{DateTime, DateTimeZone}

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.collection.mutable
import scala.concurrent.Future

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOImpl extends AuthTokenDAO {

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) = Future.successful(tokens.get(id))

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  def findExpired(dt: DateTime) = Future.successful {
    tokens.filter {
      case (_, token) =>
        val instant = Instant.ofEpochMilli(dt.getMillis)
        val zoneId = ZoneId.of(dt.getZone.getID, ZoneId.SHORT_IDS)
        val zdt = ZonedDateTime.ofInstant(instant, zoneId)
        token.expiry.isBefore(zdt)
    }.values.toSeq
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken) = {
    tokens += (token.id -> token)
    Future.successful(token)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID) = {
    tokens -= id
    Future.successful(())
  }
}

/**
 * The companion object.
 */
object AuthTokenDAOImpl {

  /**
   * The list of tokens.
   */
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
}
