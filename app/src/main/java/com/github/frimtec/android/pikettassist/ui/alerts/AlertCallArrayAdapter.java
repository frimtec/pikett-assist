package com.github.frimtec.android.pikettassist.ui.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class AlertCallArrayAdapter extends ArrayAdapter<AlertCall> {

  private static final String DATE_TIME_FORMAT = "HH:mm:ss";

  AlertCallArrayAdapter(Context context, List<AlertCall> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    AlertCall alertCall = getItem(position);
    Objects.requireNonNull(alertCall);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_call_item, parent, false);
    }
    // Lookup view for data population
    TextView receivedTime = convertView.findViewById(R.id.alert_cal_item_received_time);
    receivedTime.setText(formatDateTime(alertCall.time()));
    TextView message = convertView.findViewById(R.id.alert_cal_item_message);
    message.setText(alertCall.message());
    // Populate the data into the template view using the data object
    return convertView;
  }

  private String formatDateTime(Instant time) {
    return time != null ? LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault())) : "";
  }

}
