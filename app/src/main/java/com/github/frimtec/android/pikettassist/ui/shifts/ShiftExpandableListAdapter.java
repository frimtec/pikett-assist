package com.github.frimtec.android.pikettassist.ui.shifts;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class ShiftExpandableListAdapter extends BaseExpandableListAdapter {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMMM HH:mm";
  private static final float HOURS_PER_DAY = 24;
  private final Context context;
  private final List<Shift> shifts;

  ShiftExpandableListAdapter(Context context, List<Shift> shifts) {
    this.context = context;
    this.shifts = shifts;
  }

  @Override
  public int getGroupCount() {
    return this.shifts.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return 0;
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.shifts.get(groupPosition);
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return null;
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return groupPosition * 1_000_000L + childPosition;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    Shift shift = this.shifts.get(groupPosition);
    Objects.requireNonNull(shift);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.shift_item, parent, false);
    }
    TextView startTimeView = convertView.findViewById(R.id.shift_item_start_time);
    TextView endTimeView = convertView.findViewById(R.id.shift_item_end_time);
    TextView titleView = convertView.findViewById(R.id.shift_item_title);
    TextView durationView = convertView.findViewById(R.id.shift_item_duration);
    ImageView playIcon = convertView.findViewById(R.id.shift_item_image_play);
    playIcon.setVisibility(shift.isNow(Duration.ofSeconds(0)) ? View.VISIBLE : View.INVISIBLE);
    startTimeView.setText(String.format("%s - ", formatDateTime(shift.getStartTime())));
    endTimeView.setText(formatDateTime(shift.getEndTime()));
    titleView.setText(shift.getTitle());
    durationView.setText(String.valueOf(roundToDays(Duration.between(shift.getStartTime(), shift.getEndTime()))));
    ImageView unconfirmedIcon = convertView.findViewById(R.id.shift_item_image_unconfirmed);
    unconfirmedIcon.setVisibility(!shift.isConfirmed() ? View.VISIBLE : View.INVISIBLE);
    if (!shift.isConfirmed()) {
      titleView.setText(String.format("%s (%s)", titleView.getText(), this.context.getString(R.string.shift_item_unconfirmed)));
      titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD_ITALIC);
      convertView.setBackgroundColor(this.context.getResources().getColor(R.color.unconfirmedShift, context.getTheme()));
    }
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    return null;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return false;
  }

  static int roundToDays(Duration duration) {
    float hours = duration.toHours();
    return Math.round(hours / HOURS_PER_DAY);
  }

  private String formatDateTime(Instant time) {
    return LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault()));
  }
}
