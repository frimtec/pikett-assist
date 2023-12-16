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

import java.util.List;
import java.util.Objects;

class AlertExpandableListAdapter extends BaseExpandableListAdapter {

  private final Context context;
  private final List<Alert> alerts;

  AlertExpandableListAdapter(Context context, List<Alert> alerts) {
    this.context = context;
    this.alerts = alerts;
  }

  @Override
  public int getGroupCount() {
    return this.alerts.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return 0;
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.alerts.get(groupPosition);
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
    Alert alert = this.alerts.get(groupPosition);
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
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    return null;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return false;
  }
}
