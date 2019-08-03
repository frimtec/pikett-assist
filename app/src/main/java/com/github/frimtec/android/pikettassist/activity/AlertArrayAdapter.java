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

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMM yyyy HH:mm";
  private static final String TIME_FORMAT = "HH:mm";

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
    String timeWindowText = formatDateTime(alert.getStartTime(), DATE_TIME_FORMAT);
    if(alert.isClosed()) {
      timeWindowText = String.format("%s - %s", timeWindowText, formatDateTime(alert.getEndTime(), TIME_FORMAT));
    }
    timeWindow.setText(timeWindowText);
    String confirmText = "";
    if(alert.isConfirmed()) {
      Duration confirmDuration = Duration.between(alert.getStartTime(), alert.getConfirmTime());
      confirmText = String.format("Time to confirm: %d sec", confirmDuration.getSeconds());
    }
    String durationText = "";
    if(alert.isClosed()) {
      Duration duration = Duration.between(alert.getStartTime(), alert.getEndTime());
      durationText = String.format("Duration: %.1f min", duration.getSeconds() / 60d);
    }
    confirmTime.setText(Stream.of(confirmText, durationText).filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.joining("\n")));
    return convertView;
  }

  private String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

}
