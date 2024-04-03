package modules

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.Named
import com.redis.{RedisClient, RedisClientPool}
import iog.psg.cardano.CardanoApi
import models.daos.{AuthTokenDAO, AuthTokenDAOImpl, CardanoCacheDAO, CardanoCacheDAOImpl}
import models.services.{AuthTokenService, AuthTokenServiceImpl}
import net.ceedubs.ficus.Ficus.{intValueReader, stringValueReader, toFicusConfig}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

import scala.concurrent.ExecutionContext

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[CardanoCacheDAO].to[CardanoCacheDAOImpl]
  }

  @Provides
  def provideRedisClient(configuration: Configuration): RedisClientPool = {
    val host = configuration.underlying.as[String]("redis-host")
    val port = configuration.underlying.as[Int]("redis-port")
    val r = new RedisClientPool(host, port)
    r
  }

}
