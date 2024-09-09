package com.github.frimtec.android.pikettassist.ui.billing;

public interface BillingManagerContract {

  boolean isBillingClientReady();

  void queryPurchases();

  void destroy();
}
