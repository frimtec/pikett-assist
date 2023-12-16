package com.github.frimtec.android.pikettassist.ui.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class AlertExpandableListAdapter extends BaseExpandableListAdapter {

  private final Context context;
  private final List<YearGroup> years;

  AlertExpandableListAdapter(Context context, List<Alert> alerts) {
    this.context = context;
    Map<Integer, List<Alert>> groupedAlerts = alerts.stream()
        .collect(Collectors.groupingBy(
            alert -> LocalDateTime.ofInstant(alert.startTime(), ZoneId.systemDefault()).getYear())
        );
    this.years = groupedAlerts.keySet()
        .stream()
        .sorted(Comparator.reverseOrder())
        .map(year -> new YearGroup(year, groupedAlerts.get(year)))
        .collect(Collectors.toList());
  }

  @Override
  public int getGroupCount() {
    return this.years.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return this.years.get(groupPosition).alerts().size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.years.get(groupPosition);
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return this.years.get(groupPosition).alerts().get(childPosition);
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
    YearGroup yearGroup = this.years.get(groupPosition);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.alert_log_group, parent, false);
    }
    TextView yearText = convertView.findViewById(R.id.alert_log_group_year);
    yearText.setText(String.format(Locale.getDefault(), "%d (%d)", yearGroup.year(), yearGroup.alerts().size()));
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    Alert alert = this.years.get(groupPosition).alerts().get(childPosition);
    Objects.requireNonNull(alert);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.alert_log_item, parent, false);
    }
    ImageView playIcon = convertView.findViewById(R.id.alert_log_item_image_play);
    playIcon.setVisibility(alert.isClosed() ? View.INVISIBLE : View.VISIBLE);
    TextView timeWindow = convertView.findViewById(R.id.alert_log_item_time_window);
    TextView durations = convertView.findViewById(R.id.alert_log_item_durations);
    timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
    durations.setText(AlertViewHelper.getDurations(this.context, alert));
    return convertView;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }
}
