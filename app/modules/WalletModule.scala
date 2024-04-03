package modules

import akka.actor.ActorSystem
import com.bloxbean.cardano.client.backend.api.BackendService
import com.bloxbean.cardano.client.backend.factory.BackendFactory
import com.bloxbean.cardano.client.backend.gql.GqlBackendService
import com.bloxbean.cardano.client.backend.impl.blockfrost.common.Constants
import com.bloxbean.cardano.client.common.model.{Network, Networks}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import iog.psg.cardano.CardanoApi
import models.daos.{WalletDAO, WalletDAOImpl}
import models.services.{WalletInfoService, WalletInfoServiceImpl}
import net.ceedubs.ficus.Ficus.{stringValueReader, toFicusConfig}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext

/**
  * The base Guice module.
  */
class WalletModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[WalletInfoService].to[WalletInfoServiceImpl]
    bind[WalletDAO].to[WalletDAOImpl]
  }

  @Provides
  def provideCardanoNetwork(configuration: Configuration): Network = {
//    val mainnet: Boolean =  configuration.underlying.as[Boolean]("cardano.mainnet")
//    if(mainnet)
//      Networks.mainnet
//    else
      Networks.testnet()
  }

  @Provides
  def provideCardanoBackendService(configuration: Configuration): BackendService = {
//    val cardanoGraphql: String =  configuration.underlying.as[String]("cardano-graphql")
//    new GqlBackendService(cardanoGraphql)
    val blockfrostProjectTestId: String =  configuration.underlying.as[String]("blockfrost.project.test.id")
    BackendFactory.getBlockfrostBackendService(Constants.BLOCKFROST_TESTNET_URL, blockfrostProjectTestId)
//    val blockfrostProjectId: String =  configuration.underlying.as[String]("blockfrost.project.id")
//    BackendFactory.getBlockfrostBackendService(Constants.BLOCKFROST_MAINNET_URL, blockfrostProjectId)
  }

  @Provides
  @Named("cardano-wallet-host")
  def provideCardanoWalletHost(configuration: Configuration): String = {
    val config = configuration.underlying.as[String]("cardano-wallet-host")
    config
  }

  @Provides
  def provideCardanoApi(@Named("cardano-wallet-host") host:String)(implicit ec: ExecutionContext, as: ActorSystem): CardanoApi = {
    val api =  CardanoApi(host)
    api
  }

}
