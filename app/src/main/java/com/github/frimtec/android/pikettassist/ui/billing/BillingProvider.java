package com.github.frimtec.android.pikettassist.ui.billing;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

public interface BillingProvider {

  enum BillingState {
    NOT_LOADED,
    NOT_PURCHASED,
    PURCHASED,
    PENDING
  }

  BillingManagerContract getBillingManager();

  BillingState getBronzeSponsor();

  BillingState getSilverSponsor();

  BillingState getGoldSponsor();

  default List<BillingState> getAllProducts() {
    return Arrays.asList(getBronzeSponsor(), getSilverSponsor(), getGoldSponsor());
  }

  boolean isDonationReminderAppropriate(Context context);
}
