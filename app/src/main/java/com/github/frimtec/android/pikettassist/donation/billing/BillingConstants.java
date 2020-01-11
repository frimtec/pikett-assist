package com.github.frimtec.android.pikettassist.donation.billing;

import java.util.Arrays;
import java.util.List;

public final class BillingConstants {

  public static final String SKU_SPONSOR_BRONZE = "sponsor_bronze";
  public static final String SKU_SPONSOR_SILVER = "sponsor_silver";
  public static final String SKU_SPONSOR_GOLD = "sponsor_gold";

  private static final String[] IN_APP_SKUS = {SKU_SPONSOR_BRONZE, SKU_SPONSOR_SILVER, SKU_SPONSOR_GOLD};

  private BillingConstants() {
  }

  /**
   * Returns the list of all SKUs for the billing type specified
   */
  public static List<String> getSkuList() {
    return Arrays.asList(IN_APP_SKUS);
  }
}