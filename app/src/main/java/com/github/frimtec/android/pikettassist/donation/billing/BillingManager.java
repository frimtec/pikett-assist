package com.github.frimtec.android.pikettassist.donation.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.github.frimtec.android.pikettassist.BuildConfig;
import com.github.frimtec.android.pikettassist.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BillingManager implements PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

  private static final String TAG = "BillingManager";

  private static final Set<String> BLACKLIST = Collections.emptySet();

  /**
   * A reference to BillingClient
   **/
  private BillingClient billingClient;

  private final BillingUpdatesListener billingUpdatesListener;
  private final Activity activity;
  private final List<Purchase> purchases = new ArrayList<>();
  private boolean serviceConnected;

  @BillingResponseCode
  private int billingClientResponseCode = BillingResponseCode.SERVICE_DISCONNECTED;

  /**
   * Listener to the updates that happen when purchases list was updated or consumption of the
   * item was finished
   */
  public interface BillingUpdatesListener {

    void onBillingClientSetupFinished();

    void onPurchasesUpdated(List<Purchase> purchases);
  }

  public BillingManager(Activity activity, BillingUpdatesListener updatesListener) {
    this.activity = activity;
    this.billingUpdatesListener = updatesListener;
    this.billingClient = BillingClient.newBuilder(this.activity)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        ).build();
    startServiceConnection(() -> {
      this.billingUpdatesListener.onBillingClientSetupFinished();
      queryPurchases();
    });
  }

  @Override
  public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
    if (billingResult.getResponseCode() == BillingResponseCode.OK) {
      if (purchases != null) {
        for (Purchase purchase : purchases) {
          if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            handlePurchase(purchase);
            if (!purchase.isAcknowledged()) {
              AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
              billingClient.acknowledgePurchase(acknowledgePurchaseParams, this);
            }
          } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            handlePurchase(purchase);
          }
        }
        billingUpdatesListener.onPurchasesUpdated(this.purchases);
      }
    } else if (billingResult.getResponseCode() == BillingResponseCode.USER_CANCELED) {
      Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
    } else {
      Log.w(TAG, "onPurchasesUpdated(); resultCode: " + billingResult.getResponseCode() + "; message: " + billingResult.getDebugMessage());
    }
  }

  @Override
  public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
    if (billingResult.getResponseCode() == BillingResponseCode.OK) {
      Log.i(TAG, "onAcknowledgePurchaseResponse() - OK");
      Toast.makeText(getContext(), R.string.purchase_acknowledged, Toast.LENGTH_LONG).show();
    } else {
      Log.e(TAG, "onAcknowledgePurchaseResponse() got unknown resultCode: " + billingResult.getResponseCode());
    }
  }

  public void initiatePurchaseFlow(ProductDetailsParams productDetailsParams) {
    Runnable purchaseFlowRequest = () -> {
      BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
          .setProductDetailsParamsList(List.of(productDetailsParams)).build();
      billingClient.launchBillingFlow(activity, purchaseParams);
    };

    executeServiceRequest(purchaseFlowRequest);
  }

  private Context getContext() {
    return activity;
  }

  public void destroy() {
    if (billingClient != null && billingClient.isReady()) {
      billingClient.endConnection();
      billingClient = null;
    }
  }

  public void querySkuDetailsAsync(
      List<QueryProductDetailsParams.Product> productList,
      ProductDetailsResponseListener listener
  ) {
    Runnable queryRequest = () -> {
      QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
          .setProductList(productList)
          .build();
      billingClient.queryProductDetailsAsync(params, listener);
    };
    executeServiceRequest(queryRequest);
  }

  public int getBillingClientResponseCode() {
    return billingClientResponseCode;
  }

  private void handlePurchase(Purchase purchase) {
    if (BLACKLIST.contains(purchase.getOrderId())) {
      Log.i(TAG, "Got a blacklist purchase: " + purchase.getOrderId() + ", will be consumed.");
      billingClient.consumeAsync(ConsumeParams.newBuilder()
          .setPurchaseToken(purchase.getPurchaseToken())
          .build(), (billingResult, s) -> Log.i(TAG, "Consume respond: " + billingResult + "; " + s));
      return;
    }
    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
      Log.w(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
      return;
    }
    purchases.add(purchase);
  }

  public void queryPurchases() {
    executeServiceRequest(() ->
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            (billingResult, purchases) -> {
              if (billingResult.getResponseCode() != BillingResponseCode.OK) {
                Log.w(TAG, "Billing client result code for queryPurchasesAsync (" + billingResult.getResponseCode() + ") was bad - quitting");
                return;
              }
              this.purchases.clear();
              onPurchasesUpdated(billingResult, purchases);
            }
        )
    );
  }

  private void startServiceConnection(Runnable executeOnSuccess) {
    billingClient.startConnection(new BillingClientStateListener() {
      @Override
      public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK) {
          serviceConnected = true;
          if (executeOnSuccess != null) {
            executeOnSuccess.run();
          }
        }
        billingClientResponseCode = billingResult.getResponseCode();
      }

      @Override
      public void onBillingServiceDisconnected() {
        serviceConnected = false;
      }
    });
  }

  private void executeServiceRequest(Runnable runnable) {
    if (serviceConnected) {
      runnable.run();
    } else {
      startServiceConnection(runnable);
    }
  }

  private boolean verifyValidSignature(String signedData, String signature) {
    try {
      return Security.verifyPurchase(BuildConfig.PURCHASE_VALIDATION_KEY, signedData, signature);
    } catch (IOException e) {
      Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
      return false;
    }
  }
}