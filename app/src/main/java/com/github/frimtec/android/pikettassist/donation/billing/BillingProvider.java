package com.github.frimtec.android.pikettassist.donation.billing;

public interface BillingProvider {

  enum BillingState {
    NOT_LOADED,
    NOT_PURCHASED,
    PURCHASED,
    PENDING
  }

  BillingManager getBillingManager();

  BillingState getBronzeSponsor();

  BillingState getSilverSponsor();

  BillingState getGoldSponsor();
}
