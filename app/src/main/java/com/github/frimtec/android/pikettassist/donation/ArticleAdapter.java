package com.github.frimtec.android.pikettassist.donation;

import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class ArticleAdapter extends RecyclerView.Adapter<RowViewHolder> implements RowDataProvider {
  /**
   * Types for adapter rows
   */
  @Retention(SOURCE)
  @IntDef({TYPE_HEADER, TYPE_NORMAL})
  public @interface RowTypeDef {}
  public static final int TYPE_HEADER = 0;
  public static final int TYPE_NORMAL = 1;

  private UiManager mUiManager;
  private List<SkuRowData> mListData;

  void setUiManager(UiManager uiManager) {
    mUiManager = uiManager;
  }

  void updateData(List<SkuRowData> data) {
    mListData = data;
    notifyDataSetChanged();
  }

  @Override
  public @RowTypeDef int getItemViewType(int position) {
    return mListData == null ? TYPE_HEADER : mListData.get(position).getRowType();
  }

  @Override
  public RowViewHolder onCreateViewHolder(ViewGroup parent, @RowTypeDef int viewType) {
    return mUiManager.onCreateViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(RowViewHolder holder, int position) {
    mUiManager.onBindViewHolder(getData(position), holder);
  }

  @Override
  public int getItemCount() {
    return mListData == null ? 0 : mListData.size();
  }

  @Override
  public SkuRowData getData(int position) {
    return mListData == null ? null : mListData.get(position);
  }
}
