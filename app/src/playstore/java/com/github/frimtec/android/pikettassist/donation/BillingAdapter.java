package com.github.frimtec.android.pikettassist.donation;

import static com.github.frimtec.android.pikettassist.ui.billing.BillingProvider.BillingState.NOT_LOADED;
import static com.github.frimtec.android.pikettassist.ui.billing.BillingProvider.BillingState.NOT_PURCHASED;
import static com.github.frimtec.android.pikettassist.ui.billing.BillingProvider.BillingState.PENDING;
import static com.github.frimtec.android.pikettassist.ui.billing.BillingProvider.BillingState.PURCHASED;
import static com.github.frimtec.android.pikettassist.ui.billing.DonationReminderHelper.randomizedOn;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.android.billingclient.api.Purchase;
import com.github.frimtec.android.pikettassist.donation.billing.BillingConstants;
import com.github.frimtec.android.pikettassist.donation.billing.PlayStoreBillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.PlayStoreBillingManager.BillingUpdatesListener;
import com.github.frimtec.android.pikettassist.ui.billing.BillingManagerContract;
import com.github.frimtec.android.pikettassist.ui.billing.BillingProvider;

import java.util.List;

public class BillingAdapter implements BillingUpdatesListener, BillingProvider {

  private static final String TAG = "BillingAdapter";

  public static final String BILLING_DIALOG_TAG = "billing_dialog";


  private final FragmentActivity activity;
  private final BillingManagerContract billingManager;

  private BillingState bronzeSponsor = NOT_LOADED;
  private BillingState silverSponsor = NOT_LOADED;
  private BillingState goldSponsor = NOT_LOADED;

  public BillingAdapter(FragmentActivity activity) {
    this.activity = activity;
    this.billingManager = new PlayStoreBillingManager(activity, this);
  }

  @Override
  public BillingManagerContract getBillingManager() {
    return billingManager;
  }

  public BillingState getBronzeSponsor() {
    return bronzeSponsor;
  }

  public BillingState getSilverSponsor() {
    return silverSponsor;
  }

  public BillingState getGoldSponsor() {
    return goldSponsor;
  }

  @Override
  public boolean isDonationReminderAppropriate(Context context) {
    List<BillingState> products = getAllProducts();
    return products.stream().allMatch(billing -> billing != NOT_LOADED) &&
        products.stream().noneMatch(billing -> billing == PURCHASED) &&
        randomizedOn(context, 0.2f);
  }

  @Override
  public void onBillingClientSetupFinished() {
    DonationFragment donationFragment = (DonationFragment) activity.getSupportFragmentManager().findFragmentByTag(BILLING_DIALOG_TAG);
    if (donationFragment != null) {
      donationFragment.onManagerReady(this);
    }
  }

  @Override
  public void onPurchasesUpdated(List<Purchase> purchases) {
    bronzeSponsor = NOT_PURCHASED;
    silverSponsor = NOT_PURCHASED;
    goldSponsor = NOT_PURCHASED;
    for (Purchase purchase : purchases) {
      BillingState state = getBillingState(purchase);
      for (String productId : purchase.getProducts()) {
        switch (productId) {
          case BillingConstants.SPONSOR_BRONZE_PRODUCT_ID -> bronzeSponsor = state;
          case BillingConstants.SPONSOR_SILVER_PRODUCT_ID -> silverSponsor = state;
          case BillingConstants.SPONSOR_GOLD_PRODUCT_ID -> goldSponsor = state;
          default -> Log.e(TAG, "Has unknown product: " + productId);
        }
      }
    }
    DonationFragment donationFragment = (DonationFragment) activity.getSupportFragmentManager().findFragmentByTag(BILLING_DIALOG_TAG);
    if (donationFragment != null) {
      donationFragment.refreshUI();
    }
  }

  private BillingState getBillingState(Purchase purchase) {
    return switch (purchase.getPurchaseState()) {
      case Purchase.PurchaseState.PURCHASED -> PURCHASED;
      case Purchase.PurchaseState.PENDING -> PENDING;
      default -> NOT_PURCHASED;
    };
  }

  public void destroy() {
    this.billingManager.destroy();
  }
}
