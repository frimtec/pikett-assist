package com.github.frimtec.android.pikettassist.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.List;
import java.util.Objects;
import java.util.Set;

class TestAlarmArrayAdapter extends ArrayAdapter<TestAlarm> {

  TestAlarmArrayAdapter(Context context, List<TestAlarm> testAlarms) {
    super(context, 0, testAlarms);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    TestAlarm testAlarm = getItem(position);
    Objects.requireNonNull(testAlarm);
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.test_alarm_item, parent, false);
    }

    TextView context = convertView.findViewById(R.id.test_alarm_item_context);
    context.setText(testAlarm.getContext());

    Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(getContext());
    if(!superviseTestContexts.contains(testAlarm.getContext())) {
      context.setTextAppearance(R.style.deactivatedItem);
    }
    return convertView;
  }

}
