package com.github.frimtec.android.pikettassist.donation;

import com.android.billingclient.api.SkuDetails;
import com.github.frimtec.android.pikettassist.donation.ArticleAdapter.RowTypeDef;

public class SkuRowData {

  private final SkuDetails details;
  private final String title;
  private @RowTypeDef
  int type;

  public SkuRowData(SkuDetails details, @RowTypeDef int rowType) {
    this.details = details;
    this.title = details.getDescription();
    this.type = rowType;
  }

  public SkuRowData(String title) {
    this.details = null;
    this.title = title;
    this.type = ArticleAdapter.TYPE_HEADER;
  }

  public SkuDetails getSkuDetails() {
    return details;
  }

  public String getTitle() {
    return title;
  }

  public String getPrice() {
    return details.getPrice();
  }

  public @RowTypeDef
  int getRowType() {
    return type;
  }
}