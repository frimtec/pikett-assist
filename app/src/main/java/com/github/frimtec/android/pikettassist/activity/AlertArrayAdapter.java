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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AlertArrayAdapter extends ArrayAdapter<Alert> {

  public AlertArrayAdapter(Context context, List<Alert> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    // Get the data item for this position
    Alert alert = getItem(position);
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_log_item, parent, false);
    }
    // Lookup view for data population
    TextView timeWindow = (TextView) convertView.findViewById(R.id.time_window);
    TextView confirmTime = (TextView) convertView.findViewById(R.id.confirm_time);
    // Populate the data into the template view using the data object
    timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
    confirmTime.setText(AlertViewHelper.getDurations(alert));
    return convertView;
  }

}
