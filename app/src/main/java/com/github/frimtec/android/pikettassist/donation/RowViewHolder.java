package com.github.frimtec.android.pikettassist.donation;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.frimtec.android.pikettassist.R;

final class RowViewHolder extends RecyclerView.ViewHolder {

  TextView title, price;
  Button button;
  ImageView skuIcon;

  public interface OnButtonClickListener {

    void onButtonClicked(int position);
  }

  RowViewHolder(View itemView, OnButtonClickListener clickListener) {
    super(itemView);
    title = itemView.findViewById(R.id.title);
    price = itemView.findViewById(R.id.price);
    skuIcon = itemView.findViewById(R.id.sku_icon);
    button = itemView.findViewById(R.id.state_button);
    if (button != null) {
      button.setOnClickListener(view -> clickListener.onButtonClicked(getAdapterPosition()));
    }
  }
}