package com.github.frimtec.android.pikettassist.donation;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class CardsWithHeadersDecoration extends RecyclerView.ItemDecoration {

  private final RowDataProvider rowDataProvider;
  private final int headerGap;
  private final int rowGap;

  CardsWithHeadersDecoration(RowDataProvider rowDataProvider, int headerGap, int rowGap) {
    this.rowDataProvider = rowDataProvider;
    this.headerGap = headerGap;
    this.rowGap = rowGap;
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      RecyclerView parent,
      @NonNull RecyclerView.State state) {

    int position = parent.getChildAdapterPosition(view);
    ProductRowData data = rowDataProvider.getData(position);

    if (data.getRowType() == ArticleAdapter.TYPE_HEADER) {
      outRect.top = headerGap;
    }
    RecyclerView.Adapter<?> adapter = parent.getAdapter();
    if (adapter != null) {
      if (position == adapter.getItemCount() - 1) {
        outRect.bottom = headerGap;
      } else {
        outRect.bottom = rowGap;
      }
    }
  }
}
