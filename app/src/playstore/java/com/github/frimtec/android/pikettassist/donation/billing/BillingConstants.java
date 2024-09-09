package com.github.frimtec.android.pikettassist.donation.billing;

import static com.android.billingclient.api.BillingClient.ProductType.INAPP;

import com.android.billingclient.api.QueryProductDetailsParams.Product;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BillingConstants {

  public static final String SPONSOR_BRONZE_PRODUCT_ID = "sponsor_bronze";
  public static final String SPONSOR_SILVER_PRODUCT_ID = "sponsor_silver";
  public static final String SPONSOR_GOLD_PRODUCT_ID = "sponsor_gold";

  private static final List<Product> PRODUCTS =
      Stream.of(
          SPONSOR_BRONZE_PRODUCT_ID,
          SPONSOR_SILVER_PRODUCT_ID,
          SPONSOR_GOLD_PRODUCT_ID
      ).map(productId -> Product.newBuilder()
          .setProductId(productId)
          .setProductType(INAPP)
          .build()
      ).collect(Collectors.toList());

  private BillingConstants() {
  }

  /**
   * Returns the list of all SKUs for the billing type specified
   */
  public static List<Product> getProductList() {
    return PRODUCTS;
  }
}