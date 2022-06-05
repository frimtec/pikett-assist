package com.github.frimtec.android.pikettassist.donation;

import com.android.billingclient.api.QueryProductDetailsParams.Product;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.donation.billing.BillingConstants;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ArticleFactory {

  private final Map<String, Article> articles;

  ArticleFactory(BillingProvider provider) {
    articles = new HashMap<>();
    articles.put(BillingConstants.SPONSOR_BRONZE_PRODUCT_ID, new Article(provider, BillingProvider::getBronzeSponsor, R.drawable.bronze_icon));
    articles.put(BillingConstants.SPONSOR_SILVER_PRODUCT_ID, new Article(provider, BillingProvider::getSilverSponsor, R.drawable.silver_icon));
    articles.put(BillingConstants.SPONSOR_GOLD_PRODUCT_ID, new Article(provider, BillingProvider::getGoldSponsor, R.drawable.gold_icon));
  }

  List<Product> getProductList() {
    return BillingConstants.getProductList();
  }

  void onBindViewHolder(ProductRowData data, RowViewHolder holder) {
    Article article = articles.get(data.getProductDetails().getProductId());
    Objects.requireNonNull(article);
    article.onBindViewHolder(data, holder);
  }

  void onButtonClicked(ProductRowData data) {
    Article article = articles.get(data.getProductDetails().getProductId());
    Objects.requireNonNull(article);
    article.onButtonClicked(data);
  }
}
