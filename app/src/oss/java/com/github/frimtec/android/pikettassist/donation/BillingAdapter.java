package com.github.frimtec.android.pikettassist.donation;

import static com.github.frimtec.android.pikettassist.ui.billing.BillingProvider.BillingState.NOT_PURCHASED;
import static com.github.frimtec.android.pikettassist.ui.billing.DonationReminderHelper.randomizedOn;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.github.frimtec.android.pikettassist.donation.billing.PaypalBillingManager;
import com.github.frimtec.android.pikettassist.ui.billing.BillingManagerContract;
import com.github.frimtec.android.pikettassist.ui.billing.BillingProvider;


public class BillingAdapter implements BillingProvider {

  public static final String BILLING_DIALOG_TAG = "billing_dialog";

  private final BillingManagerContract billingManager;

  private final BillingState bronzeSponsor = NOT_PURCHASED;
  private final BillingState silverSponsor = NOT_PURCHASED;
  private final BillingState goldSponsor = NOT_PURCHASED;

  public BillingAdapter(@SuppressWarnings("unused") FragmentActivity activity) {
    this.billingManager = new PaypalBillingManager();
  }

  @Override
  public BillingManagerContract getBillingManager() {
    return billingManager;
  }

  public BillingState getBronzeSponsor() {
    return bronzeSponsor;
  }

  @Override
  public boolean isDonationReminderAppropriate(Context context) {
    return randomizedOn(context, 0.05f);
  }

  public BillingState getSilverSponsor() {
    return silverSponsor;
  }

  public BillingState getGoldSponsor() {
    return goldSponsor;
  }

  public void destroy() {
    this.billingManager.destroy();
  }
}
