package models

import com.bloxbean.cardano.client.backend.model.{AddressContent, Asset, TxContentOutputAmount}
import play.api.libs.json.{Json, OFormat}
import utils.Utils

import java.util.UUID
import scala.jdk.javaapi.CollectionConverters


case class WalletSummary(
                          userWalletId:UUID,
                          userId:UUID,
                          name:String,
                          cardanoWalletAccountDetails:GetWalletResponse
                        )

object WalletSummary {
  implicit val f: OFormat[WalletSummary] = Json.format[WalletSummary]
}

case class ContentOutputAmount(  unit: String,
                                 quantity: String,
                                 assetDetails: Option[CardanoAssetDetails]
                              ){
  def name:String = {
    Utils.getAssetNameFromUnit(unit)
  }
}

object ContentOutputAmount {
  implicit val f: OFormat[ContentOutputAmount] = Json.format[ContentOutputAmount]
  def fromTxContentOutputAmount(list:java.util.List[TxContentOutputAmount], assets:List[Asset]): Seq[ContentOutputAmount] = {
    CollectionConverters.asScala(list).toSeq.map(item => {
      println(assets)
      val assetDetailOpt = assets.find(asset => asset.getPolicyId+asset.getAssetName == item.getUnit) .map(a => CardanoAssetDetails(
        policyId = a.getPolicyId,
        assetName = Utils.hexToString(a.getAssetName),
        fingerprint = a.getFingerprint,
        quantity = a.getQuantity,
        initialMintTxHash = a.getInitialMintTxHash
      ))
      ContentOutputAmount(unit = item.getUnit, quantity = item.getQuantity, assetDetails = assetDetailOpt)
    })
  }
}

case class AddressContents(
                            stakeAddress: String,
                            addressType: String,
                            contentOutputAmount: Seq[ContentOutputAmount]
                          )
object AddressContents {
  implicit val f: OFormat[AddressContents] = Json.format[AddressContents]
  def fromAddressContent(addressContent: Option[AddressContent], assets:List[Asset]): Option[AddressContents] = {
    addressContent.map(addressContent =>
      AddressContents(
        stakeAddress = addressContent.getStakeAddress,
        addressType = addressContent.getType.getValue,
        contentOutputAmount = ContentOutputAmount.fromTxContentOutputAmount(addressContent.getAmount, assets)
      )
    )
  }
}

case class CardanoWalletAccountDetails(
                                        mnemonic: String,
                                        baseAddress: String,
                                        enterpriseAddress: String
                                      )
object CardanoWalletAccountDetails {
  implicit val f: OFormat[CardanoWalletAccountDetails] = Json.format[CardanoWalletAccountDetails]
}


case class CardanoAssetDetails(
                                policyId: String,
                                assetName: String,
                                fingerprint: String,
                                quantity: String,
                                initialMintTxHash: String
                              )
object CardanoAssetDetails {
  implicit val f: OFormat[CardanoAssetDetails] = Json.format[CardanoAssetDetails]
}
