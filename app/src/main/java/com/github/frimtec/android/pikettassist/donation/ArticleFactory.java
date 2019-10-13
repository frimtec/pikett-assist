package com.github.frimtec.android.pikettassist.donation;

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
    articles.put(BillingConstants.SKU_SPONSOR_BRONZE, new Article(provider, BillingProvider::getBronzeSponsor, R.drawable.bronze_icon));
    articles.put(BillingConstants.SKU_SPONSOR_SILVER, new Article(provider, BillingProvider::getSilverSponsor, R.drawable.silver_icon));
    articles.put(BillingConstants.SKU_SPONSOR_GOLD, new Article(provider, BillingProvider::getGoldSponsor, R.drawable.gold_icon));
  }

  final List<String> getSkuList() {
    return BillingConstants.getSkuList();
  }

  void onBindViewHolder(SkuRowData data, RowViewHolder holder) {
    Article article = articles.get(data.getSkuDetails().getSku());
    Objects.requireNonNull(article);
    article.onBindViewHolder(data, holder);
  }

  void onButtonClicked(SkuRowData data) {
    Article article = articles.get(data.getSkuDetails().getSku());
    Objects.requireNonNull(article);
    article.onButtonClicked(data);
  }
}
