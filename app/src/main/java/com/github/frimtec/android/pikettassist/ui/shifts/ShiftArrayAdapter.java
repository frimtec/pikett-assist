package com.github.frimtec.android.pikettassist.ui.shifts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class ShiftArrayAdapter extends ArrayAdapter<Shift> {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMMM HH:mm";

  ShiftArrayAdapter(Context context, List<Shift> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Shift shift = getItem(position);
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
