package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.util.List;
import java.util.Objects;
import java.util.Set;

class TestAlarmArrayAdapter extends ArrayAdapter<TestAlarmContext> {

  TestAlarmArrayAdapter(Context context, List<TestAlarmContext> testAlarmContexts) {
    super(context, 0, testAlarmContexts);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    TestAlarmContext testAlarmContext = getItem(position);
    Objects.requireNonNull(testAlarmContext);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.test_alarm_item, parent, false);
    }

    TextView textView = convertView.findViewById(R.id.test_alarm_item_context);
    textView.setText(testAlarmContext.getContext());

    Set<TestAlarmContext> supervisedTestAlarmContexts = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
    if(!supervisedTestAlarmContexts.contains(testAlarmContext)) {
      textView.setTextAppearance(R.style.deactivatedItem);
    }
    return convertView;
  }

}
