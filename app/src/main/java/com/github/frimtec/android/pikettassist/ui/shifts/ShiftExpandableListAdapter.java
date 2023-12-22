package com.github.frimtec.android.pikettassist.ui.shifts;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class ShiftExpandableListAdapter extends AbstractExpandableListAdapter<YearMonth, Shift> {

  private static final String DATE_TIME_FORMAT = "EEEE, dd. MMMM HH:mm";
  private static final float HOURS_PER_DAY = 24;

  ShiftExpandableListAdapter(Context context, List<Shift> shifts) {
    super(
        context,
        shifts,
        shift -> YearMonth.from(LocalDateTime.ofInstant(shift.getStartTime(), ZoneId.systemDefault())), Comparator.naturalOrder()
    );
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    Group<YearMonth, ? extends Shift> yearMonthGroup = getGroupedItems().get(groupPosition);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.general_list_group_item, parent, false);
    }
    TextView title = convertView.findViewById(R.id.general_list_group_item_title);
    title.setText(
        String.format(
            Locale.getDefault(), "%s (%d)", DateTimeFormatter.ofPattern(
                "yyyy MMMM",
                Locale.getDefault()
            ).format(yearMonthGroup.key()), yearMonthGroup.items().size()
        )
    );
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    Shift shift = this.getGroupedItems().get(groupPosition).items().get(childPosition);
    Objects.requireNonNull(shift);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.shift_item, parent, false);
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
      titleView.setText(String.format("%s (%s)", titleView.getText(), getContext().getString(R.string.shift_item_unconfirmed)));
      titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD_ITALIC);
      convertView.setBackgroundColor(getContext().getResources().getColor(R.color.unconfirmedShift, getContext().getTheme()));
    }
    return convertView;
  }

  static int roundToDays(Duration duration) {
    float hours = duration.toHours();
    return Math.round(hours / HOURS_PER_DAY);
  }

  private String formatDateTime(Instant time) {
    return LocalDateTime.ofInstant(time, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT, Locale.getDefault()));
  }
}
