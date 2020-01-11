package com.github.frimtec.android.pikettassist.donation;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.donation.billing.BillingProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DonationFragment extends DialogFragment {

  private static final String TAG = "DonationFragment";

  private RecyclerView recyclerView;
  private ArticleAdapter adapter;
  private View loadingView;
  private TextView errorTextView;
  private BillingProvider billingProvider;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.acquire_fragment, container, false);
    errorTextView = root.findViewById(R.id.error_textview);
    recyclerView = root.findViewById(R.id.list);
    loadingView = root.findViewById(R.id.screen_wait);
    if (billingProvider != null) {
      handleManagerAndUiReady();
    }
    // Setup a toolbar for this fragment
    Toolbar toolbar = root.findViewById(R.id.toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_up);
    toolbar.setNavigationOnClickListener(v -> dismiss());
    toolbar.setTitle(R.string.menu_donate);
    return root;
  }

  public void refreshUI() {
    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
  }

  /**
   * Notifies the fragment that billing manager is ready and provides a BillingProviders
   * instance to access it
   */
  public void onManagerReady(BillingProvider billingProvider) {
    this.billingProvider = billingProvider;
    if (recyclerView != null) {
      handleManagerAndUiReady();
    }
  }

  private void setWaitScreen(boolean set) {
    recyclerView.setVisibility(set ? View.GONE : View.VISIBLE);
    loadingView.setVisibility(set ? View.VISIBLE : View.GONE);
  }

  private void handleManagerAndUiReady() {
    setWaitScreen(true);
    querySkuDetails();
  }

  private void displayAnErrorIfNeeded() {
    if (getActivity() == null || getActivity().isFinishing()) {
      return;
    }

    loadingView.setVisibility(View.GONE);
    errorTextView.setVisibility(View.VISIBLE);
    int billingResponseCode = billingProvider.getBillingManager().getBillingClientResponseCode();

    if (billingResponseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
      errorTextView.setText(getText(R.string.error_billing_unavailable));
    } else {
      errorTextView.setText(getText(R.string.error_billing_default));
    }
  }

  private void querySkuDetails() {
    if (getActivity() != null && !getActivity().isFinishing()) {
      final List<SkuRowData> dataList = new ArrayList<>();
      adapter = new ArticleAdapter();
      final UiManager uiManager = createUiManager(adapter, billingProvider);
      adapter.setUiManager(uiManager);
      // Once we added all the subscription items, fill the in-app items rows below
      List<String> inAppSkus = uiManager.getDelegatesFactory().getSkuList();
      addSkuRows(dataList, inAppSkus);
    }
  }

  private void addSkuRows(List<SkuRowData> inList, List<String> skusList) {
    billingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP, skusList,
        (billingResult, skuDetailsList) -> {

          if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Unsuccessful query. Error code: " + billingResult.getResponseCode());
          } else if (skuDetailsList != null && skuDetailsList.size() > 0) {
            // If we successfully got SKUs, add a header in front of the row
            inList.add(new SkuRowData(getString(R.string.header_inapp)));
            // Then fill all the other rows

            skuDetailsList.stream().sorted(Comparator.comparing(SkuDetails::getOriginalPriceAmountMicros).reversed()).forEach(skuDetails ->
                inList.add(new SkuRowData(skuDetails)));
            if (inList.size() == 0) {
              displayAnErrorIfNeeded();
            } else {
              if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(adapter);
                Context context = getContext();
                if (context != null) {
                  Resources res = getContext().getResources();
                  recyclerView.addItemDecoration(new CardsWithHeadersDecoration(adapter, (int) res.getDimension(R.dimen.header_gap), (int) res.getDimension(R.dimen.row_gap)));
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
              }
              adapter.updateData(inList);
              setWaitScreen(false);
            }
          } else {
            displayAnErrorIfNeeded();
          }
        });
  }

  private UiManager createUiManager(ArticleAdapter adapter, BillingProvider provider) {
    return new UiManager(adapter, provider);
  }
}