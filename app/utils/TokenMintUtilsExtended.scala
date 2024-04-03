package utils

import co.nstant.in.cbor.CborDecoder
import co.nstant.in.cbor.model.Map
import com.bloxbean.cardano.client.account.Account
import com.bloxbean.cardano.client.backend.api.helper.impl.DefaultUtxoSelectionStrategyImpl
import com.bloxbean.cardano.client.backend.api.helper.{FeeCalculationService, TransactionHelperService}
import com.bloxbean.cardano.client.backend.api.{BackendService, BlockService, TransactionService}
import com.bloxbean.cardano.client.backend.exception.{ApiException, InsufficientBalanceException}
import com.bloxbean.cardano.client.backend.model.{Amount, ProtocolParams, Utxo}
import com.bloxbean.cardano.client.common.CardanoConstants.LOVELACE
import com.bloxbean.cardano.client.common.MinAdaCalculator
import com.bloxbean.cardano.client.common.model.Network
import com.bloxbean.cardano.client.crypto.{KeyGenUtil, Keys, SecretKey, VerificationKey}
import com.bloxbean.cardano.client.jna.CardanoJNAUtil
import com.bloxbean.cardano.client.metadata.cbor.{CBORMetadata, CBORMetadataMap}
import com.bloxbean.cardano.client.transaction.model.{MintTransaction, PaymentTransaction, TransactionDetailsParams}
import com.bloxbean.cardano.client.transaction.spec.script.{RequireTimeBefore, ScriptAll, ScriptPubkey}
import com.bloxbean.cardano.client.transaction.spec._
import com.bloxbean.cardano.client.util.{AssetUtil, HexUtil, JsonUtil, Tuple}
import models.CreateNftAsset

import java.math.BigInteger
import java.util
import java.util.Optional
import scala.concurrent.ExecutionContext


class TokenMintUtilsExtended(network: Network, backendService: BackendService)(implicit executionContext: ExecutionContext) {

  val blockService: BlockService = backendService.getBlockService
  val addressService = backendService.getAddressService
  val utxoService = backendService.getUtxoService
  val epochService = backendService.getEpochService
  val transactionService: TransactionService = backendService.getTransactionService
  val transactionHelperService: TransactionHelperService = backendService.getTransactionHelperService
  val feeCalculationService: FeeCalculationService = backendService.getFeeCalculationService(transactionHelperService)
  val utxoTransactionBuilder = backendService.getUtxoTransactionBuilder
  private val utxoSelectionStrategy = new DefaultUtxoSelectionStrategyImpl(utxoService)

  def getTtl: Long = {
    val block = blockService.getLastestBlock
    println(block)
    val blockV = blockService.getLastestBlock.getValue
    val slot = blockV.getSlot
    slot + 200000
  }

  def getHalfSignedTxString(receiverAddress:Seq[String],
                createNftAsset:CreateNftAsset):String = {

    val receiver = receiverAddress.head
    //    println(senderMnemonic)
    println(receiver)

    val keys: Keys = KeyGenUtil.generateKey
    val vkey: VerificationKey = keys.getVkey
    val skey: SecretKey = keys.getSkey
    println(vkey)

    val scriptPubkey: ScriptPubkey = ScriptPubkey.create(vkey)

    val requireTimeBefore = new RequireTimeBefore(getTtl)

    val scriptAll = new ScriptAll()
      .addScript(requireTimeBefore)
      .addScript(scriptPubkey)

    val policyId: String = scriptAll.getPolicyId
    //    val policyId: String = scriptPubkey.getPolicyId

    val assetName = createNftAsset.name

    val asset = new Asset(assetName, BigInteger.valueOf(1))

    val metadata: CBORMetadata = new CBORMetadata()
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

    //    val utxoList = List(new Utxo())
    val mintTransaction: MintTransaction =
      MintTransaction.builder()
        //        .sender(senderAccount)
        .receiver(receiver)
        .mintAssets(util.Arrays.asList(multiAsset))
        .policyScript(scriptAll)
        //        .policyScript(scriptPubkey)
        //        .utxosToInclude()
        .policyKeys(util.Arrays.asList(skey))
        .build()

    println(mintTransaction)

    val detailsParams: TransactionDetailsParams =
      TransactionDetailsParams.builder()
        .ttl(getTtl)
        .build()

    println("detailsParams")
    println(detailsParams)



    //    //Calculate fee
    //    val fee = feeCalculationService.calculateFee(mintTransaction, detailsParams, metadata)


    //     //Calculate fee
    val protocolParamsResult = epochService.getProtocolParameters
    if (!protocolParamsResult.isSuccessful) throw new ApiException("Unable to fetch protocol parameters to calculate the fee")
    val protocolParams: ProtocolParams = protocolParamsResult.getValue

    if (mintTransaction.getFee == null || mintTransaction.getFee.compareTo(BigInteger.valueOf(170000)) == -1) { //Just a dummy fee
      mintTransaction.setFee(new BigInteger("170000")) //Set a min fee just for calcuation purpose if not set
    }

    println(JsonUtil.getPrettyJson(mintTransaction))


    //  /  //Calculate fee
    val bodyTemp: TransactionBody = getTransactionBody(mintTransaction, receiver, receiverAddress, detailsParams, protocolParams)
    val fee = getFees(mintTransaction, metadata, bodyTemp, protocolParams)
    mintTransaction.setFee(fee)
    val body: TransactionBody = getTransactionBody(mintTransaction, receiver, receiverAddress, detailsParams, protocolParams)
    body.setFee(fee)


    val transaction: Transaction = Transaction.builder.body(body).metadata(metadata).build

    val transactionWitnessSet = new TransactionWitnessSet
    transactionWitnessSet.getNativeScripts.add(mintTransaction.getPolicyScript)
    transaction.setWitnessSet(transactionWitnessSet)
    transaction.setMetadata(metadata)

    var halfSignedTxn = transaction.serializeToHex

    if (mintTransaction.getPolicyKeys != null) {
      mintTransaction.getPolicyKeys.forEach(key => {
        halfSignedTxn = CardanoJNAUtil.signWithSecretKey(halfSignedTxn, HexUtil.encodeHexString(key.getBytes))
      })
    }

    if (mintTransaction.getAdditionalWitnessAccounts != null) {
      mintTransaction.getAdditionalWitnessAccounts.forEach(addWitnessAcc => {
        halfSignedTxn = addWitnessAcc.sign(halfSignedTxn)
      })
    }

    println("halfSignedTxn")
    println(halfSignedTxn)
    halfSignedTxn
  }

    // TODO SIGN

    def submitTxn(haflSignedTxnSentToUser:String,
                  signedWitnessSetFromBrowser:String):String = {

//    val haflSignedTxnSentToUser = "83a600818258203dc278f498ed7f5bac893d689b86a1de80ac9be72cf9de3d18e257bb19c192af000182825839008797cd07642cea82df6e42b7dbe4e39cd476521fe8dc566ed3e25bec00a33722fe79eefc63048e40688070e744283c7a377e15c4837dcb6c1a3b6af5be825839008797cd07642cea82df6e42b7dbe4e39cd476521fe8dc566ed3e25bec00a33722fe79eefc63048e40688070e744283c7a377e15c4837dcb6c821a00150bd0a1581cef3bf437f4dca953e40b7aa5f3765746e1316b235cc61b7c38fd47d1a1497465737450686f746f01021a0002de51031a031947b00758203ccbb72c613aadf6e5a44ec4cf1d5bb3cb3f958682a00be33c4e664b833459ff09a1581cef3bf437f4dca953e40b7aa5f3765746e1316b235cc61b7c38fd47d1a1497465737450686f746f01a200818258201a15d7eb9a406d72c9b53b7720a6847812da44f5a162f648ee101618ad0d73e55840f77063c3f903c58c4672924f0648658d6e8ce376423c9125d5708469203117f7905a39b59e44dc7be074c312d1ad1b641d7943a0ece4e20cce76d3fec1504d02018182018282051a031947b08200581cf927f9ee1d1adcb5ee6ea021e644ba3b153856b148125660c0f7415aa11902d1a178386566336266343337663464636139353365343062376161356633373635373436653133313662323335636336316237633338666434376431a1697465737450686f746fa4646e616d65697465737450686f746f65696d6167657835697066733a2f2f516d64676850457878424d52684d5a3559656b6b6a3556716a554d56616b583969694d714e623853446854796a746b6465736372697074696f6e697465737450686f746f66617574686f7264416e6f6e"
//    val signedWitnessSetFromBrowser = "a10081825820b82de6c4cea6db79defdc850681382c2072fcd4f443b2d34553ea8c1bf7531d05840098e756afb2e81bdc70db94967ea90903dbc4ec421a62b2a1e7390a10d9fac3ed7ac113eee8ed6d0811e81a67bf27a2223c45822d9d59708e6ac5264eb7e2d0e" //TODO get signed tx from browser

    val dataItemList = CborDecoder.decode(HexUtil.decodeHexString(signedWitnessSetFromBrowser))


    val transactionWitnessSetFromSigned = TransactionWitnessSet.deserialize(dataItemList.get(0).asInstanceOf[Map])

    val transaction3 = Transaction.deserialize(HexUtil.decodeHexString(haflSignedTxnSentToUser))
//    val existingWitnessSet = transaction3.getWitnessSet
//    existingWitnessSet.getVkeyWitnesses.addAll(transactionWitnessSetFromSigned.getVkeyWitnesses)

//    transactionWitnessSetFromSigned.setNativeScripts(existingWitnessSet.getNativeScripts)

    println(transactionWitnessSetFromSigned)

    println("transactionWitnessSetFromSigned: "+JsonUtil.getPrettyJson(transactionWitnessSetFromSigned))
    println("transaction3: "+JsonUtil.getPrettyJson(transaction3.getWitnessSet))
    transaction3.getWitnessSet.getVkeyWitnesses.addAll(transactionWitnessSetFromSigned.getVkeyWitnesses)
    println("transaction3: "+JsonUtil.getPrettyJson(transaction3.getWitnessSet))
    println(JsonUtil.getPrettyJson(transaction3))

    println("Fully Signed Txn : " + transaction3.serializeToHex())

    val result = transactionService.submitTransaction(HexUtil.decodeHexString(transaction3.serializeToHex()))
    println(result)

    result.getResponse

  }

  private def getTransactionBody(mintTransaction: MintTransaction, receiver:String, receiverAddress:Seq[String],
                                 detailsParams: TransactionDetailsParams, protocolParams: ProtocolParams): TransactionBody = {

    val minAmount: BigInteger = createDummyOutputAndCalculateMinAdaForTxnOutput(receiver, mintTransaction.getMintAssets, protocolParams)
    //getMinimumLovelaceForMultiAsset(detailsParams).multiply(BigInteger.valueOf(totalAssets));
    val totalCost: BigInteger = minAmount.add(mintTransaction.getFee)

    //Get utxos from the transaction request if available
    var utxos: util.List[Utxo] = mintTransaction.getUtxosToInclude

    println("utxos")
    println(utxos)

//    import scala.collection.JavaConverters._
    import scala.jdk.CollectionConverters.ListHasAsScala
    import scala.jdk.CollectionConverters.SeqHasAsJava

    //If no utxos found as part of request, then fetch from backend
    if (utxos == null || utxos.size == 0) {
      utxos = receiverAddress.flatMap(r => getUtxos(r, LOVELACE, totalCost).asScala).asJava
      println("new utxos")
      println(utxos)

      if (utxos.size == 0) {
        throw new InsufficientBalanceException("Not enough utxos found to cover balance : " + totalCost + " lovelace")
      }
    }

    val inputs: util.List[TransactionInput] = new util.ArrayList[TransactionInput]
    val outputs: util.List[TransactionOutput] = new util.ArrayList[TransactionOutput]

    //Create single TxnOutput for the sender
    val transactionOutput: TransactionOutput = new TransactionOutput
    transactionOutput.setAddress(receiver)
    val senderValue: Value = Value.builder.coin(BigInteger.ZERO).multiAssets(new util.ArrayList[MultiAsset]).build
    transactionOutput.setValue(senderValue)

    //Keep a flag to make sure fee is already deducted
    val feeDeducted: Boolean = false
    utxos.forEach(utxo => { //create input for this utxo
      val transactionInput: TransactionInput = new TransactionInput(utxo.getTxHash, utxo.getOutputIndex)
      inputs.add(transactionInput)
      copyUtxoValuesToChangeOutput(transactionOutput, utxo)
      /*  //Deduct fee from sender's output if applicable
                  BigInteger lovelaceValue = transactionOutput.getValue().getCoin();
                  if(feeDeducted) { //fee + min amount required for new multiasset output
                      transactionOutput.getValue().setCoin(lovelaceValue);
                  } else {
                      BigInteger remainingAmount = lovelaceValue.subtract(amount);
                      if(remainingAmount.compareTo(BigInteger.ZERO) == 1) { //Positive value
                          transactionOutput.getValue().setCoin(remainingAmount); //deduct requirement amt (fee + min amount)
                          feeDeducted = true;
                      }
                  }*/
    })

    //Deduct fee + minCost in a MA output
    val remainingAmount: BigInteger = transactionOutput.getValue.getCoin.subtract(totalCost)

    val commission = BigInteger.valueOf(2000000L)

    transactionOutput.getValue.setCoin(remainingAmount.subtract(commission)) //deduct requirement amt (fee + min amount)


    //Check if minimum Ada is not met. Topup
    //Transaction will fail if minimun ada not there. So try to get some additional utxos
    verifyMinAdaInOutputAndUpdateIfRequired(inputs, transactionOutput, detailsParams, utxos, protocolParams)

    outputs.add(transactionOutput)


    //Commission
    val transactionOutputCommission: TransactionOutput = new TransactionOutput
    //    transactionOutputCommission.setAddress(receiver)
    transactionOutputCommission.setAddress("addr_test1qqg3y55v0jy9hlgzqsj032lc7waxjrgx3qq7x2pwupe2699s9nwgyj0q3jm3wsx7z9h7qnuwusgy04dtclprgvvpn0qshcmwhu")
    val commissionValue: Value = Value.builder.coin(commission).build
    transactionOutputCommission.setValue(commissionValue)

    outputs.add(transactionOutputCommission)

    //Check if minimum Ada is not met. Topup
    //Transaction will fail if minimum ada not there. So try to get some additional utxos
    verifyMinAdaInOutputAndUpdateIfRequired(inputs, transactionOutput, detailsParams, utxos, protocolParams)


    //Create a separate output for minted assets
    //Create output
    val mintedTransactionOutput: TransactionOutput = new TransactionOutput
    mintedTransactionOutput.setAddress(receiver)
    val value: Value = Value.builder.coin(minAmount).multiAssets(new util.ArrayList[MultiAsset]).build
    mintedTransactionOutput.setValue(value)
    mintTransaction.getMintAssets.forEach(ma => {
      mintedTransactionOutput.getValue.getMultiAssets.add(ma)
    })
    outputs.add(mintedTransactionOutput)



    val body: TransactionBody = TransactionBody.builder.inputs(inputs).outputs(outputs).fee(mintTransaction.getFee).ttl(detailsParams.getTtl).validityStartInterval(detailsParams.getValidityStartInterval).mint(mintTransaction.getMintAssets).build

    println(JsonUtil.getPrettyJson(body))
    body
  }

  def getFees(mintTransaction: MintTransaction, metadata: CBORMetadata, body: TransactionBody, protocolParams: ProtocolParams): BigInteger = {

    val transaction: Transaction = Transaction.builder.body(body).metadata(metadata).build

    val transactionWitnessSet = new TransactionWitnessSet
    transactionWitnessSet.getNativeScripts.add(mintTransaction.getPolicyScript)
    transaction.setWitnessSet(transactionWitnessSet)
    transaction.setMetadata(metadata)

    val fakeSenderMnemonicTEST = "brown detect clever farm cable mobile during bird rude minor lion robot mail play lunch sentence device finish portion push into turkey onion coral"
    val fakeSenderAccount: Account = new Account(network, fakeSenderMnemonicTEST)

    var signedTxn = fakeSenderAccount.sign(transaction)

    if (mintTransaction.getPolicyKeys != null) {
      mintTransaction.getPolicyKeys.forEach(key => {
        signedTxn = CardanoJNAUtil.signWithSecretKey(signedTxn, HexUtil.encodeHexString(key.getBytes))
      })
    }

    if (mintTransaction.getAdditionalWitnessAccounts != null) {
      mintTransaction.getAdditionalWitnessAccounts.forEach(addWitnessAcc => {
        signedTxn = addWitnessAcc.sign(signedTxn)
      })
    }

    println("fake Signed Txn : " + signedTxn)

    val bytes = HexUtil.decodeHexString(signedTxn)
    val fee =  BigInteger.valueOf((protocolParams.getMinFeeA * bytes.length).toLong + protocolParams.getMinFeeB.toLong)

    fee
  }

  private def createDummyOutputAndCalculateMinAdaForTxnOutput(address: String, multiAssets: util.List[MultiAsset], protocolParams: ProtocolParams): BigInteger = {
    val txnOutput = new TransactionOutput
    //Dummy address
    txnOutput.setAddress(address)
    txnOutput.setValue(new Value(BigInteger.ZERO, multiAssets))
    new MinAdaCalculator(protocolParams).calculateMinAda(txnOutput)
  }

  @throws[ApiException]
  private def getUtxos(address: String, unit: String, amount: BigInteger, excludeUtxos: util.Set[Utxo] = new util.HashSet[Utxo]()): util.List[Utxo] = {
//    try {
      utxoSelectionStrategy.selectUtxos(address, unit, amount, excludeUtxos)
//    }catch {
//      case ex: ApiException => util.List.of()
//    }
  }

  private def copyUtxoValuesToChangeOutput(changeOutput: TransactionOutput, utxo: Utxo): Unit = {
    utxo.getAmount.forEach((utxoAmt: Amount) => {
      def foo(utxoAmt: Amount) = { //For each amt in utxo
        val utxoUnit: String = utxoAmt.getUnit
        val utxoQty: BigInteger = utxoAmt.getQuantity
        if (utxoUnit == LOVELACE) {
          var existingCoin: BigInteger = changeOutput.getValue.getCoin
          if (existingCoin == null) {
            existingCoin = BigInteger.ZERO
          }
          changeOutput.getValue.setCoin(existingCoin.add(utxoQty))
        }
        else {
          val policyIdAssetName: Tuple[String, String] = AssetUtil.getPolicyIdAndAssetName(utxoUnit)
          //Find if the policy id is available
          val multiAssetOptional: Optional[MultiAsset] = changeOutput.getValue.getMultiAssets.stream.filter((ma: MultiAsset) => policyIdAssetName._1 == ma.getPolicyId).findFirst
          if (multiAssetOptional.isPresent) {
            val assetOptional: Optional[Asset] = multiAssetOptional.get.getAssets.stream.filter((ast: Asset) => policyIdAssetName._2 == ast.getName).findFirst
            if (assetOptional.isPresent) {
              val changeVal: BigInteger = assetOptional.get.getValue.add(utxoQty)
              assetOptional.get.setValue(changeVal)
            }
            else {
              val asset: Asset = new Asset(policyIdAssetName._2, utxoQty)
              multiAssetOptional.get.getAssets.add(asset)
            }
          }
          else {
            val asset: Asset = new Asset(policyIdAssetName._2, utxoQty)
            val multiAsset: MultiAsset = new MultiAsset(policyIdAssetName._1, new util.ArrayList[Asset](util.Arrays.asList(asset)))
            changeOutput.getValue.getMultiAssets.add(multiAsset)
          }
        }
      }

      foo(utxoAmt)
    })
    //Remove any empty MultiAssets
    val multiAssets: util.List[MultiAsset] = changeOutput.getValue.getMultiAssets
    val markedForRemoval: util.List[MultiAsset] = new util.ArrayList[MultiAsset]
    if (multiAssets != null && multiAssets.size > 0) {
      multiAssets.forEach((ma: MultiAsset) => {
        def foo(ma: MultiAsset) = {
          if (ma.getAssets == null || ma.getAssets.size == 0) {
            markedForRemoval.add(ma)
          }
        }

        foo(ma)
      })
      multiAssets.removeAll(markedForRemoval)
    }
  }

  @throws[ApiException]
  private def verifyMinAdaInOutputAndUpdateIfRequired(inputs: util.List[TransactionInput], transactionOutput: TransactionOutput, detailsParams: TransactionDetailsParams, excludeUtxos: util.List[Utxo], protocolParams: ProtocolParams): Unit = {
    var minRequiredLovelaceInOutput: BigInteger = new MinAdaCalculator(protocolParams).calculateMinAda(transactionOutput)
    println("minRequiredLovelaceInOutput")
    println(minRequiredLovelaceInOutput)
    //Create another copy of the list
    val ignoreUtxoList: util.List[Utxo] = new util.ArrayList[Utxo](excludeUtxos.size())
    util.Collections.copy(excludeUtxos, ignoreUtxoList)
    println("ignoreUtxoList")
    println(ignoreUtxoList)
    println("transactionOutput")
    println(transactionOutput)
    while ( {
      transactionOutput.getValue.getCoin != null && minRequiredLovelaceInOutput.compareTo(transactionOutput.getValue.getCoin) == 1
    }) { //Get utxos
      val additionalUtxos: util.List[Utxo] = getUtxos(transactionOutput.getAddress, LOVELACE, minRequiredLovelaceInOutput, new util.HashSet[Utxo](ignoreUtxoList))
      if (additionalUtxos == null || additionalUtxos.size == 0) {
        println("Not enough utxos found to cover minimum lovelace in an output")
        // TODO Add break condition
        throw new RuntimeException("Not enough utxos found to cover minimum lovelace in an output")
      } else {
        println("Additional Utoxs found: " + additionalUtxos)
        additionalUtxos.forEach(addUtxo => {
          val addTxnInput: TransactionInput = TransactionInput.builder.transactionId(addUtxo.getTxHash).index(addUtxo.getOutputIndex).build
          inputs.add(addTxnInput)
          //Update change output
          copyUtxoValuesToChangeOutput(transactionOutput, addUtxo)
        })
        ignoreUtxoList.addAll(additionalUtxos)
        //Calculate final minReq balance in output, if still doesn't satisfy, continue again
        minRequiredLovelaceInOutput = new MinAdaCalculator(protocolParams).calculateMinAda(transactionOutput)
      }
    }
  }

}


