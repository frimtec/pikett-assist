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

import java.util.List;
import java.util.Objects;
import java.util.Set;

class TestAlarmExpandableListAdapter extends BaseExpandableListAdapter {

  private final Context context;
  private final List<TestAlarmContext> testAlarmContexts;

  TestAlarmExpandableListAdapter(Context context, List<TestAlarmContext> testAlarmContexts) {
    this.context = context;
    this.testAlarmContexts = testAlarmContexts;
  }


  @Override
  public int getGroupCount() {
    return this.testAlarmContexts.size();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return 0;
  }

  @Override
  public Object getGroup(int groupPosition) {
    return this.testAlarmContexts.get(groupPosition);
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
    TestAlarmContext testAlarmContext = this.testAlarmContexts.get(groupPosition);
    Objects.requireNonNull(testAlarmContext);
    if (convertView == null) {
      convertView = LayoutInflater.from(this.context).inflate(R.layout.test_alarm_item, parent, false);
    }

    TextView textView = convertView.findViewById(R.id.test_alarm_item_context);
    textView.setText(testAlarmContext.context());

    Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(this.context);
    if (!supervisedTestAlarmContexts.contains(testAlarmContext)) {
      textView.setTextAppearance(R.style.deactivatedItem);
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

}
