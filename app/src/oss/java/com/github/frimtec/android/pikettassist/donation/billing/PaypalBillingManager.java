package com.github.frimtec.android.pikettassist.donation.billing;

import com.github.frimtec.android.pikettassist.ui.billing.BillingManagerContract;

public class PaypalBillingManager implements BillingManagerContract {

  public PaypalBillingManager() {
  }

  @Override
  public boolean isBillingClientReady() {
    return true;
  }

  public void destroy() {
  }

  public void queryPurchases() {
  }
}