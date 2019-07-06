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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

class PikettShiftArrayAdapter extends ArrayAdapter<PikettShift> {
  public PikettShiftArrayAdapter(Context context, List<PikettShift> shifts) {
    super(context, 0, shifts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    // Get the data item for this position
    PikettShift shift = getItem(position);
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.shift_item, parent, false);
    }
    // Lookup view for data population
    TextView startTimeView = (TextView) convertView.findViewById(R.id.startTime);
    TextView endTimeView = (TextView) convertView.findViewById(R.id.endTime);
    TextView titleView = (TextView) convertView.findViewById(R.id.title);
    // Populate the data into the template view using the data object
    startTimeView.setText(LocalDateTime.ofInstant(shift.getStartTime(false), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
    endTimeView.setText(LocalDateTime.ofInstant(shift.getEndTime(false), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())));
    titleView.setText(shift.getTitle());
    // Return the completed view to render on screen
    return convertView;
  }
}
