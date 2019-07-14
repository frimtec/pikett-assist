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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class AlertArrayAdapter extends ArrayAdapter<Alert> {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM HH:mm";

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
    TextView startTime = (TextView) convertView.findViewById(R.id.start_time);
    TextView confirmTime = (TextView) convertView.findViewById(R.id.confirm_time);
    TextView endTime = (TextView) convertView.findViewById(R.id.end_time);
    // Populate the data into the template view using the data object
    startTime.setText(formatDateTime(alert.getStartTime()));
    confirmTime.setText(formatDateTime(alert.getConfirmTime()));
    endTime.setText(formatDateTime(alert.getEndTime()));
    // Return the completed view to render on screen
    return convertView;
  }

  private String formatDateTime(Instant time) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }

}
