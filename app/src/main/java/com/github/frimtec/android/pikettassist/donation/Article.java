package com.github.frimtec.android.pikettassist.donation;

import androidx.annotation.DrawableRes;

import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.ProductDetails;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState;

import java.util.function.Function;

import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.NOT_PURCHASED;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PENDING;
import static com.github.frimtec.android.pikettassist.donation.billing.BillingProvider.BillingState.PURCHASED;

final class Article {

  private final BillingProvider billingProvider;
  private final Function<BillingProvider, BillingState> billingState;
  @DrawableRes
  private final int icon;

  Article(BillingProvider billingProvider, Function<BillingProvider, BillingState> billingState, @DrawableRes int icon) {
    this.billingProvider = billingProvider;
    this.billingState = billingState;
    this.icon = icon;
  }

  void onBindViewHolder(ProductRowData data, RowViewHolder holder) {
    holder.price.setText(data.getPrice());
    holder.button.setEnabled(true);
    BillingState billingState = this.billingState.apply(billingProvider);
    if(NOT_PURCHASED == billingState) {
      holder.button.setEnabled(true);
      holder.button.setText(R.string.button_buy);
    } else if(PURCHASED == billingState) {
      holder.button.setEnabled(false);
      holder.button.setText(R.string.button_own);
    } else if(PENDING == billingState) {
      holder.button.setEnabled(false);
      holder.button.setText(R.string.button_pending);
    }
    holder.skuIcon.setImageResource(icon);
  }

  void onButtonClicked(ProductRowData data) {
    if (data != null && NOT_PURCHASED == billingState.apply(billingProvider)) {
      ProductDetails productDetails = data.getProductDetails();
      billingProvider.getBillingManager().initiatePurchaseFlow(
          ProductDetailsParams.newBuilder()
              .setProductDetails(productDetails)
              .build()
      );
    }
  }
}