package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractExpandableListAdapter;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class TestAlarmExpandableListAdapter extends AbstractExpandableListAdapter<Boolean, TestAlarm> {

  TestAlarmExpandableListAdapter(Context context, List<TestAlarm> testAlarmContexts) {
    super(
        context,
        testAlarmContexts,
        testAlarm -> ApplicationPreferences.instance().getSupervisedTestAlarms(context).contains(testAlarm.context()),
        Comparator.reverseOrder()
    );
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
    var stateGroup = getGroupedItems().get(groupPosition);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.general_list_group_item, parent, false);
    }
    TextView title = convertView.findViewById(R.id.general_list_group_item_title);
    String stateString = getContext().getString(stateGroup.key() ? R.string.general_enabled : R.string.general_disabled);
    title.setText(String.format(Locale.getDefault(), "%s (%d)", stateString, stateGroup.items().size()));
    return convertView;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
    TestAlarm testAlarm = getGroupedItems().get(groupPosition).items().get(childPosition);
    Objects.requireNonNull(testAlarm);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.test_alarm_item, parent, false);
    }

    TextView textView = convertView.findViewById(R.id.test_alarm_item_context);
    textView.setText(TextUtils.isEmpty(testAlarm.alias()) ? testAlarm.context().context() : String.format(Locale.getDefault(), "%s / %s", testAlarm.context(), testAlarm.alias()));
    return convertView;
  }

}
