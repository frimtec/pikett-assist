package com.github.frimtec.android.pikettassist.donation;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class CardsWithHeadersDecoration extends RecyclerView.ItemDecoration {

  private final RowDataProvider mRowDataProvider;
  private final int mHeaderGap, mRowGap;

  public CardsWithHeadersDecoration(RowDataProvider rowDataProvider, int headerGap,
                                    int rowGap) {
    this.mRowDataProvider = rowDataProvider;
    this.mHeaderGap = headerGap;
    this.mRowGap = rowGap;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                             RecyclerView.State state) {

    final int position = parent.getChildAdapterPosition(view);
    final SkuRowData data = mRowDataProvider.getData(position);

    // We should add a space on top of every header card
    if (data.getRowType() == ArticleAdapter.TYPE_HEADER) {
      outRect.top = mHeaderGap;
    }

    // Adding a space under the last item
    if (position == parent.getAdapter().getItemCount() - 1) {
      outRect.bottom = mHeaderGap;
    } else {
      outRect.bottom = mRowGap;
    }
  }
}
