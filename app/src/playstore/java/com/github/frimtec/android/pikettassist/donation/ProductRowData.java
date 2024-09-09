package com.github.frimtec.android.pikettassist.donation;

import com.android.billingclient.api.ProductDetails;
import com.github.frimtec.android.pikettassist.donation.ArticleAdapter.RowTypeDef;

import java.util.Objects;

public class ProductRowData {

  private final ProductDetails details;
  private final String title;
  @RowTypeDef
  private final int type;

  ProductRowData(ProductDetails details) {
    this.details = details;
    this.title = details.getDescription();
    this.type = ArticleAdapter.TYPE_NORMAL;
  }

  ProductRowData(String title) {
    this.details = null;
    this.title = title;
    this.type = ArticleAdapter.TYPE_HEADER;
  }

  ProductDetails getProductDetails() {
    return details;
  }

  public String getTitle() {
    return title;
  }

  String getPrice() {
    return Objects.requireNonNull(details.getOneTimePurchaseOfferDetails()).getFormattedPrice();
  }

  @RowTypeDef
  int getRowType() {
    return type;
  }
}