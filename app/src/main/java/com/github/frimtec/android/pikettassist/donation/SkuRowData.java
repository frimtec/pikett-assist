package com.github.frimtec.android.pikettassist.donation;

import com.android.billingclient.api.SkuDetails;
import com.github.frimtec.android.pikettassist.donation.ArticleAdapter.RowTypeDef;

public class SkuRowData {

  private final SkuDetails details;
  private final String title;
  @RowTypeDef
  private final int type;

  SkuRowData(SkuDetails details) {
    this.details = details;
    this.title = details.getDescription();
    this.type = ArticleAdapter.TYPE_NORMAL;
  }

  SkuRowData(String title) {
    this.details = null;
    this.title = title;
    this.type = ArticleAdapter.TYPE_HEADER;
  }

  SkuDetails getSkuDetails() {
    return details;
  }

  public String getTitle() {
    return title;
  }

  String getPrice() {
    return details.getPrice();
  }

  @RowTypeDef
  int getRowType() {
    return type;
  }
}