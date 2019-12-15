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

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class AlertCallArrayAdapter extends ArrayAdapter<AlertCall> {

  private static final String DATE_TIME_FORMAT = "HH:mm:ss";

  AlertCallArrayAdapter(Context context, List<AlertCall> shifts) {
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
    TextView receivedTime = convertView.findViewById(R.id.alert_cal_item_received_time);
    receivedTime.setText(formatDateTime(alertCall.getTime(), DATE_TIME_FORMAT));
    TextView message = convertView.findViewById(R.id.alert_cal_item_message);
    message.setText(alertCall.getMessage());
    // Populate the data into the template view using the data object
    return convertView;
  }

  private String formatDateTime(Instant time, String format) {
    return time != null ?
        LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(format, Locale.getDefault())) : "";
  }

}
