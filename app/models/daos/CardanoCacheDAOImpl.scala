package models.daos

import com.redis.{RedisClient, RedisClientPool}
import models.{AddressesDetailsResponse, GetWalletAddressesResponse, GetWalletResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CardanoCacheDAOImpl  @Inject()(
                                     clients:RedisClientPool
  )(implicit ec: ExecutionContext) extends CardanoCacheDAO  {

  override def addAddressesEntry(walletId: String, getWalletAddressesResponses: Seq[GetWalletAddressesResponse], addressesDetails: Seq[AddressesDetailsResponse]): Future[Unit] =
    Future {
      clients.withClient(redisClient => {
        val addresses = getWalletAddressesResponses.map(_.id)
        val enterpriseAddresses = addressesDetails.flatMap(_.spending_key_hash_bech32).distinct
        println("addressesDetails")
        println(addressesDetails)
        val stakeAddress = addressesDetails.flatMap(_.stake_key_hash_bech32).head
        addresses ++ enterpriseAddresses match {
          case head :: Nil => redisClient.sadd(s"wallet:$walletId:addresses", head)
          case head :: tail => redisClient.sadd(s"wallet:$walletId:addresses", head, tail: _ *)
          case _ => ()
        }
        redisClient.sadd(s"wallet:$walletId:stakeAddress", stakeAddress)
        ()
      })
    }

  override def getAddresses(walletId: String): Future[Seq[String]] =
    Future {
      clients.withClient(redisClient => {
        redisClient.smembers(s"wallet:$walletId:addresses").map(_.flatten).getOrElse(Set.empty).toSeq
      })
    }


}
