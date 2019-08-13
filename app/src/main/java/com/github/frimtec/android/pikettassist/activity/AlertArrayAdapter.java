package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;

import java.util.List;

class AlertArrayAdapter extends ArrayAdapter<Alert> {

  public AlertArrayAdapter(Context context, List<Alert> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Alert alert = getItem(position);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_log_item, parent, false);
    }
    TextView timeWindow = (TextView) convertView.findViewById(R.id.alert_log_item_time_window);
    TextView durations = (TextView) convertView.findViewById(R.id.alert_log_item_durations);
    timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
    durations.setText(AlertViewHelper.getDurations(getContext(), alert));
    return convertView;
  }

}
