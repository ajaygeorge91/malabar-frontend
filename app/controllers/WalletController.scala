package controllers

import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.impl.providers.GoogleTotpInfo
import forms.{CreateAssetForm, ImportWalletForm, SubmitCreateAssetForm}
import models.{CreateNftAsset, ImportWalletFromPublicKey}
import models.services.WalletInfoService
import play.api.libs.json.Json
import play.api.mvc._
import utils.route.Calls

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * The basic application controller.
 */
class WalletController @Inject()(
  scc: SilhouetteControllerComponents,
  walletsPage: views.html.wallets,
  publicWalletDetailsPage: views.html.publicWalletDetails,
  importPublicWalletPage: views.html.importPublicWallet,
  mintAssetPage: views.html.mintAsset,
  walletInfoService: WalletInfoService,
)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def wallets = SecuredAction.async { implicit request  =>
    for{
      publicWallets <- walletInfoService.getPublicWalletsForUser(request.identity.userID)
      publicWalletWithDetails <- Future.sequence(publicWallets.map(publicWallet =>
        walletInfoService.getPublicWalletDetails(publicWallet.userWalletId).map(summary => publicWallet.copy(walletSummery =Some(summary)))))
      privateWallets <- walletInfoService.getPrivateWalletsForUser(request.identity.userID)
      privateWalletWithDetails <- Future.sequence(privateWallets.map(privateWallet =>
        walletInfoService.getPrivateWalletDetails(privateWallet.userWalletId).map(summary => privateWallet.copy(walletSummery =Some(summary)))))
    } yield {
      Ok(walletsPage(request.identity, publicWalletWithDetails, privateWalletWithDetails))
    }
  }

  def publicWalletDetails(userWalletId:UUID) = SecuredAction.async { implicit request  =>
    for{
      publicWalletWithDetails <- walletInfoService.getPublicWalletDetails(userWalletId)
      transactions <- walletInfoService.getWalletTransactions(publicWalletWithDetails.cardanoWalletAccountDetails.id)
    } yield {
      if(publicWalletWithDetails.userId.equals(request.identity.userID)) {
        Ok(publicWalletDetailsPage(request.identity, publicWalletWithDetails, transactions))
      }else{
        NotFound
      }
    }
  }

  def addPublicWalletPage = SecuredAction { implicit request =>
    Ok(importPublicWalletPage(request.identity, ImportWalletForm.form))
  }

  def addPublicWallet = SecuredAction.async { implicit request =>
    ImportWalletForm.form.bindFromRequest().fold(
    form => Future.successful(BadRequest(importPublicWalletPage(request.identity, form))),
    data => {
      walletInfoService.addPublicWalletForUser(request.identity.userID,ImportWalletFromPublicKey(data.name, data.publicKey)).map{ uw =>
        Redirect(controllers.routes.WalletController.publicWalletDetails(uw.userWalletId))
      }
    })
  }

  def mintAsset = SecuredAction { implicit request =>
    val createAssetForm = CreateAssetForm.form.fill(CreateAssetForm.Data(name = "TestName1",
      description = "TestDescription1",
      image = "ipfs://QmdghPExxBMRhMZ5Yekkj5VqjUMVakX9iiMqNb8SDhTyjt",
      author = "TestAuthor",
      addresses = ""))
      createAssetForm("transactionToSign").value.getOrElse("")
    val submitCreateAssetForm = SubmitCreateAssetForm.form
    Ok(mintAssetPage(request.identity, createAssetForm, submitCreateAssetForm))
  }

  def mintAsset1 = SecuredAction.async { implicit request =>
    CreateAssetForm.form.bindFromRequest().fold(
      form => {
        val submitCreateAssetForm = SubmitCreateAssetForm.form
        Future.successful(BadRequest(mintAssetPage(request.identity, form, submitCreateAssetForm)))
      },
      data => {
        walletInfoService.createAsset(request.identity.userID,data).map{ transactionToSign =>
          val formFilled = CreateAssetForm.form.fill(data)
          val submitFormFilled = SubmitCreateAssetForm.form.fill(SubmitCreateAssetForm.Data(name = Some(data.name),
            description = Some(data.description),
            image = Some(data.image),
            author = Some(data.author),
            addresses = Some(data.addresses),
            transactionToSign = transactionToSign,
            witnessSet = ""))
          Ok(mintAssetPage(request.identity, formFilled, submitFormFilled))
        }
      })
  }

  def mintAssetSubmit1 = SecuredAction.async { implicit request =>
    SubmitCreateAssetForm.form.bindFromRequest().fold(
      form => {
        val formFilled = CreateAssetForm.form
        Future.successful(BadRequest(mintAssetPage(request.identity, formFilled, form)))
      },
      data => {
        val formFilled = CreateAssetForm.form
        println(data)
        walletInfoService.submitCreateAsset(data.transactionToSign,data.witnessSet).map{ transactionId =>
          Ok(transactionId)
        }
      })
  }

}
