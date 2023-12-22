package com.github.frimtec.android.pikettassist.ui.alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class AlertExpandableListAdapter extends AbstractExpandableListAdapter<Integer, Alert> {

  AlertExpandableListAdapter(Context context, List<Alert> alerts) {
    super(
        context,
        alerts,
        alert -> LocalDateTime.ofInstant(alert.startTime(), ZoneId.systemDefault()).getYear(), Comparator.reverseOrder()
    );
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    Group<Integer, ? extends Alert> yearGroup = getGroupedItems().get(groupPosition);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.general_list_group_item, parent, false);
    }
    TextView title = convertView.findViewById(R.id.general_list_group_item_title);
    title.setText(String.format(Locale.getDefault(), "%d (%d)", yearGroup.key(), yearGroup.items().size()));
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    Alert alert = getGroupedItems().get(groupPosition).items().get(childPosition);
    Objects.requireNonNull(alert);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.alert_log_item, parent, false);
    }
    ImageView playIcon = convertView.findViewById(R.id.alert_log_item_image_play);
    playIcon.setVisibility(alert.isClosed() ? View.INVISIBLE : View.VISIBLE);
    TextView timeWindow = convertView.findViewById(R.id.alert_log_item_time_window);
    TextView durations = convertView.findViewById(R.id.alert_log_item_durations);
    timeWindow.setText(AlertViewHelper.getTimeWindow(alert));
    durations.setText(AlertViewHelper.getDurations(getContext(), alert));
    return convertView;
  }
}
