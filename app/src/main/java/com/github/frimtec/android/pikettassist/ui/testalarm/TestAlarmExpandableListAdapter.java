package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class TestAlarmExpandableListAdapter extends BaseExpandableListAdapter {

  private final Context context;
  private final List<StateGroup> stateGroups;


  TestAlarmExpandableListAdapter(Context context, List<TestAlarmContext> testAlarmContexts) {
    this.context = context;
    Set<TestAlarmContext> supervisedTestAlarms = ApplicationPreferences.instance().getSupervisedTestAlarms(this.context);
    Map<Boolean, List<TestAlarmContext>> groupedTestAlarmContexts = testAlarmContexts.stream()
        .collect(Collectors.partitioningBy(supervisedTestAlarms::contains));

    this.stateGroups = groupedTestAlarmContexts.keySet()
        .stream()
        .sorted(Comparator.reverseOrder())
        .map(state -> new StateGroup(state, groupedTestAlarmContexts.get(state)))
        .collect(Collectors.toList());
  }


  @Override
  public int getGroupCount() {
    return this.stateGroups.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return this.stateGroups.get(groupPosition).testAlarmContexts().size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.stateGroups.get(groupPosition);
  }

  @Override
  public Object getChild(int groupPosition, int childPosition) {
    return this.stateGroups.get(groupPosition).testAlarmContexts().get(childPosition);
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
    StateGroup stateGroup = this.stateGroups.get(groupPosition);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.general_list_group_item, parent, false);
    }
    TextView title = convertView.findViewById(R.id.general_list_group_item_title);
    String stateString = context.getString(stateGroup.active() ? R.string.general_enabled : R.string.general_disabled);
    title.setText(String.format(Locale.getDefault(), "%s (%d)", stateString, stateGroup.testAlarmContexts().size()));
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    TestAlarmContext testAlarmContext = this.stateGroups.get(groupPosition).testAlarmContexts().get(childPosition);
    Objects.requireNonNull(testAlarmContext);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.test_alarm_item, parent, false);
    }

    TextView textView = convertView.findViewById(R.id.test_alarm_item_context);
    textView.setText(testAlarmContext.context());

    return convertView;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

}
