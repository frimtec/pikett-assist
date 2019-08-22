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
import com.github.frimtec.android.pikettassist.domain.PikettShift;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class PikettShiftArrayAdapter extends ArrayAdapter<PikettShift> {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMMM HH:mm";

  PikettShiftArrayAdapter(Context context, List<PikettShift> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    PikettShift shift = getItem(position);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.shift_item, parent, false);
    }
    TextView startTimeView = convertView.findViewById(R.id.shift_item_start_time);
    TextView endTimeView = convertView.findViewById(R.id.shift_item_end_time);
    TextView titleView = convertView.findViewById(R.id.shift_item_title);
    startTimeView.setText(String.format("%s - ", formatDateTime(shift.getStartTime(false))));
    endTimeView.setText(formatDateTime(shift.getEndTime(false)));
    titleView.setText(shift.getTitle());
    return convertView;
  }

  private String formatDateTime(Instant time) {
    return LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault()));
  }
}
