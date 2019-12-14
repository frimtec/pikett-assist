package com.github.frimtec.android.pikettassist.donation.billing;

import java.util.Arrays;
import java.util.List;

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

  default List<BillingState> getAllProducts() {
    return Arrays.asList(getBronzeSponsor(), getSilverSponsor(), getGoldSponsor());
  }
}
