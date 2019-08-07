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
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class AlertCallArrayAdapter extends ArrayAdapter<AlertCall> {

  private static final String DATE_TIME_FORMAT = "HH:mm:ss";

  public AlertCallArrayAdapter(Context context, List<AlertCall> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    // Get the data item for this position
    AlertCall alertCall = getItem(position);
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_call_item, parent, false);
    }
    // Lookup view for data population
    TextView receivedTime = (TextView) convertView.findViewById(R.id.alert_cal_item_received_time);
    receivedTime.setText(formatDateTime(alertCall.getTime(), DATE_TIME_FORMAT));
    TextView message = (TextView) convertView.findViewById(R.id.alert_cal_item_message);
    message.setText(alertCall.getMessage());
    // Populate the data into the template view using the data object
    return convertView;
  }

  private String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

}
