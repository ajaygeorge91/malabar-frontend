package utils

import com.bloxbean.cardano.client.account.Account
import com.bloxbean.cardano.client.backend.api.helper.{FeeCalculationService, TransactionHelperService}
import com.bloxbean.cardano.client.backend.api.{BackendService, BlockService}
import com.bloxbean.cardano.client.common.model.{Network, Networks}
import com.bloxbean.cardano.client.crypto.{KeyGenUtil, Keys, SecretKey, VerificationKey}
import com.bloxbean.cardano.client.metadata.cbor.{CBORMetadata, CBORMetadataList, CBORMetadataMap}
import com.bloxbean.cardano.client.transaction.model.{MintTransaction, PaymentTransaction, TransactionDetailsParams}
import com.bloxbean.cardano.client.transaction.spec.script.{RequireTimeBefore, ScriptAll, ScriptAtLeast, ScriptPubkey}
import com.bloxbean.cardano.client.transaction.spec.{Asset, MultiAsset}

import java.math.BigInteger
import java.util
import scala.concurrent.{ExecutionContext, Future}
import com.bloxbean.cardano.client.crypto.SecretKey
import com.bloxbean.cardano.client.crypto.VerificationKey
import com.bloxbean.cardano.client.util.JsonUtil
import com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE
import models.CreateNftAsset



class TokenMintUtils(network: Network, backendService: BackendService)(implicit executionContext: ExecutionContext) {

  val blockService: BlockService = backendService.getBlockService
  val addressService = backendService.getAddressService
  val utxoService = backendService.getUtxoService
  val epochService = backendService.getEpochService
  val transactionHelperService: TransactionHelperService = backendService.getTransactionHelperService
  val feeCalculationService: FeeCalculationService = backendService.getFeeCalculationService(transactionHelperService)

  def getTtl: Long = {
    val block = blockService.getLastestBlock.getValue
    val slot = block.getSlot
    slot + 2000
  }

  def mintToken(senderMnemonic:String,
                receiverAddress:Option[String],
                createNftAsset:CreateNftAsset) = {

    //    val receiverAddress:String = "addr_test1qqwpl7h3g84mhr36wpetk904p7fchx2vst0z696lxk8ujsjyruqwmlsm344gfux3nsj6njyzj3ppvrqtt36cp9xyydzqzumz82"

    //    val senderMnemonicTEST = "brown detect clever farm cable mobile during bird rude minor lion robot mail play lunch sentence device finish portion push into turkey onion coral"
    //    val senderAccount: Account = new Account(network, senderMnemonicTEST)
    val senderAccount: Account = new Account(network, senderMnemonic)

    //    val receiver = receiverAddress.getOrElse(senderAccount.enterpriseAddress())
    val receiver = receiverAddress.getOrElse("addr1q9krzzeu2tgrcxveq42e4w96pkfkk6q5s9lrv2u445qvj5wy6fpptqdlmgqjreu869slxvj9m9yfjpky8zqgw963dmmqnhn0w5")
    println(senderMnemonic)
    println(receiver)
    println(senderAccount.baseAddress())
    println(senderAccount.enterpriseAddress())

    val prvKeyBytes = senderAccount.privateKeyBytes
    val pubKeyBytes = senderAccount.publicKeyBytes

    val skey = SecretKey.create(prvKeyBytes)
    val vkey = VerificationKey.create(pubKeyBytes)

    //    val keys: Keys = KeyGenUtil.generateKey
    //    val vkey: VerificationKey = keys.getVkey
    //    val skey: SecretKey = keys.getSkey

    val scriptPubkey: ScriptPubkey = ScriptPubkey.create(vkey)

    val requireTimeBefore = new RequireTimeBefore(getTtl)

    val scriptAll = new ScriptAll()
      .addScript(requireTimeBefore)
      .addScript(scriptPubkey)

    val policyId: String = scriptAll.getPolicyId
    //    val policyId: String = scriptPubkey.getPolicyId

    val assetName = createNftAsset.name

    val asset = new Asset(assetName, BigInteger.valueOf(1))

    val metadata = new CBORMetadata()
      .put(new BigInteger("721"), new CBORMetadataMap()
        .put(policyId,
          new CBORMetadataMap()
            .put(assetName,
              new CBORMetadataMap()
                .put("name", createNftAsset.name)
                .put("image", createNftAsset.image)
                .put("description", createNftAsset.description)
                .put("author", createNftAsset.author)
            )
        )
      )

    val multiAsset: MultiAsset =
      MultiAsset.builder()
        .policyId(policyId)
        .assets(util.Arrays.asList(asset))
        .build()

    println(multiAsset)

    val mintTransaction: MintTransaction =
      MintTransaction.builder()
        .sender(senderAccount)
        .receiver(receiver)
        .mintAssets(util.Arrays.asList(multiAsset))
        .policyScript(scriptAll)
        //        .policyScript(scriptPubkey)
        .policyKeys(util.Arrays.asList(skey))
        .build()

    println(mintTransaction)

    val detailsParams: TransactionDetailsParams =
      TransactionDetailsParams.builder()
        .ttl(getTtl)
        .build()

    println("detailsParams")
    println(detailsParams)

    //Calculate fee
    val fee = feeCalculationService.calculateFee(mintTransaction, detailsParams, metadata)

    //    val fee = BigInteger.valueOf(1l)
    //    val fee = BigInteger.valueOf(186709l)
    println(fee)
    mintTransaction.setFee(fee)

    val str = transactionHelperService.createSignedMintTransaction(mintTransaction, detailsParams, metadata)
    println(str)

    //    val result = transactionHelperService.mintToken(mintTransaction, detailsParams, metadata)
    //    println(result)
    //
    //    println("Request: \n" + JsonUtil.getPrettyJson(mintTransaction))
    //    if (result.isSuccessful) println("Transaction Id: " + result.getValue)
    //    else println("Transaction failed: " + result)

  }


  //  def paymentTransaction(senderMnemonic:String,
  //                         receiverAddress:Option[String]) = {
  //
  ////    val senderMnemonic = "thought genre manage bitter special library surprise dance laundry bulk blanket slogan tray exchange book vast bean march slice floor lunar now spare detail"
  //    val sender: Account = new Account(network, senderMnemonic)
  //    val receiver = "addr1q9fksa3lg0s2g43apu0vzf67qqaryfdh2glanej2ukwwq59rmhxxmzdez0yx5vksefvmrzj0y45txe66rc43rfkvx9kslyqaa4"
  //
  //    println(senderMnemonic)
  //    println(sender.baseAddress())
  //    println(sender.enterpriseAddress())
  //
  //    val  paymentTransaction =
  //      PaymentTransaction.builder()
  //        .sender(sender)
  //        .receiver(receiver)
  //        .amount(BigInteger.valueOf(1000000))
  //        .unit(LOVELACE)
  //        .build()
  //
  //
  //    val detailsParams: TransactionDetailsParams =
  //      TransactionDetailsParams.builder()
  //        .ttl(getTtl)
  //        .build()
  //
  //    val res1 = addressService.getAddressInfo(sender.baseAddress())
  //    println(res1)
  //
  //    val utxos = utxoService.getUtxos(sender.baseAddress(),100,1)
  //    println(utxos)
  //
  //    println("Request: \n" + JsonUtil.getPrettyJson(paymentTransaction))
  //
  //    val result = transactionHelperService.transfer(paymentTransaction, detailsParams)
  //    println(result)
  //
  //    println("Request: \n" + JsonUtil.getPrettyJson(paymentTransaction))
  //    if (result.isSuccessful) println("Transaction Id: " + result.getValue)
  //    else println("Transaction failed: " + result)
  //
  //  }
}


