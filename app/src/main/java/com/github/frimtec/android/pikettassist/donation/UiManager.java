package com.github.frimtec.android.pikettassist.donation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;

class UiManager implements RowViewHolder.OnButtonClickListener {

  private final RowDataProvider rowDataProvider;
  private final ArticleFactory articleFactory;

  UiManager(RowDataProvider rowDataProvider, BillingProvider billingProvider) {
    this.rowDataProvider = rowDataProvider;
    articleFactory = new ArticleFactory(billingProvider);
  }

  ArticleFactory getDelegatesFactory() {
    return articleFactory;
  }

  final RowViewHolder onCreateViewHolder(ViewGroup parent, @ArticleAdapter.RowTypeDef int viewType) {
    if (viewType == ArticleAdapter.TYPE_HEADER) {
      View item = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.sku_details_row_header, parent, false);
      return new RowViewHolder(item, this);
    } else {
      View item = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.sku_details_row, parent, false);
      return new RowViewHolder(item, this);
    }
  }

  void onBindViewHolder(ProductRowData data, RowViewHolder holder) {
    if (data != null) {
      holder.title.setText(data.getTitle());
      // For non-header rows we need to feel other data and init button's state
      if (data.getRowType() != ArticleAdapter.TYPE_HEADER) {
        articleFactory.onBindViewHolder(data, holder);
      }
    }
  }

  public void onButtonClicked(int position) {
    ProductRowData data = rowDataProvider.getData(position);
    if (data != null) {
      articleFactory.onButtonClicked(data);
    }
  }
}