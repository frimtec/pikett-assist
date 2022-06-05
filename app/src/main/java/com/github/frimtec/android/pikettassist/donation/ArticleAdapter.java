package com.github.frimtec.android.pikettassist.donation;

import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
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
  @interface RowTypeDef {}
  static final int TYPE_HEADER = 0;
  static final int TYPE_NORMAL = 1;

  private UiManager uiManager;
  private List<ProductRowData> listData;

  void setUiManager(UiManager uiManager) {
    this.uiManager = uiManager;
  }

  void updateData(List<ProductRowData> data) {
    listData = data;
    notifyDataSetChanged();
  }

  @Override
  @RowTypeDef
  public int getItemViewType(int position) {
    return listData == null ? TYPE_HEADER : listData.get(position).getRowType();
  }

  @Override
  @NonNull
  public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @RowTypeDef int viewType) {
    return uiManager.onCreateViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
    uiManager.onBindViewHolder(getData(position), holder);
  }

  @Override
  public int getItemCount() {
    return listData == null ? 0 : listData.size();
  }

  @Override
  public ProductRowData getData(int position) {
    return listData == null ? null : listData.get(position);
  }
}
