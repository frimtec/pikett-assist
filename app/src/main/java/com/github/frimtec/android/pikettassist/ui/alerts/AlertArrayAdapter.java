package com.github.frimtec.android.pikettassist.ui.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;

import java.util.List;
import java.util.Objects;

class AlertArrayAdapter extends ArrayAdapter<Alert> {

  AlertArrayAdapter(Context context, List<Alert> alerts) {
    super(context, 0, alerts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Alert alert = getItem(position);
    Objects.requireNonNull(alert);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_log_item, parent, false);
    }
    ImageView playIcon = convertView.findViewById(R.id.alert_log_item_image_play);
    playIcon.setVisibility(alert.isClosed() ? View.INVISIBLE : View.VISIBLE);
    TextView timeWindow = convertView.findViewById(R.id.alert_log_item_time_window);
    TextView durations = convertView.findViewById(R.id.alert_log_item_durations);
    timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
    durations.setText(AlertViewHelper.getDurations(getContext(), alert));
    return convertView;
  }

}
