package com.github.frimtec.android.pikettassist.activity;

import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.android.billingclient.api.Purchase;
import com.github.frimtec.android.pikettassist.donation.DonationFragment;
import com.github.frimtec.android.pikettassist.donation.billing.BillingConstants;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager;
import com.github.frimtec.android.pikettassist.donation.billing.BillingManager.BillingUpdatesListener;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;

import java.util.List;

import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_LOADED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_PURCHASED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PENDING;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;

class BillingAdapter implements BillingUpdatesListener, BillingProvider {

  private static final String TAG = "BillingAdapter";

  static final String BILLING_DIALOG_TAG = "billing_dialog";


  private final FragmentActivity activity;
  private final BillingManager billingManager;

  private BillingState bronzeSponsor = NOT_LOADED;
  private BillingState silverSponsor = NOT_LOADED;
  private BillingState goldSponsor = NOT_LOADED;

  BillingAdapter(FragmentActivity activity) {
    this.activity = activity;
    this.billingManager = new BillingManager(activity, this);
  }

  @Override
  public BillingManager getBillingManager() {
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
      switch (purchase.getSku()) {
        case BillingConstants.SKU_SPONSOR_BRONZE:
          bronzeSponsor = state;
          break;
        case BillingConstants.SKU_SPONSOR_SILVER:
          silverSponsor = state;
          break;
        case BillingConstants.SKU_SPONSOR_GOLD:
          goldSponsor = state;
          break;
        default:
          Log.e(TAG, "Has unknown product: " + purchase.getSku());
      }
    }
    DonationFragment donationFragment = (DonationFragment) activity.getSupportFragmentManager().findFragmentByTag(BILLING_DIALOG_TAG);
    if (donationFragment != null) {
      donationFragment.refreshUI();
    }
  }

  private BillingState getBillingState(Purchase purchase) {
    switch (purchase.getPurchaseState()) {
      case Purchase.PurchaseState.PURCHASED:
        return PURCHASED;
      case Purchase.PurchaseState.PENDING:
        return PENDING;
      case Purchase.PurchaseState.UNSPECIFIED_STATE:
      default:
        return NOT_PURCHASED;
    }
  }

  void destroy() {
    this.billingManager.destroy();
  }
}
