package com.github.frimtec.android.pikettassist.ui.preferences;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.Set;
import java.util.stream.Collectors;

public class TestContextMultiSelectListPreference extends MultiSelectListPreference {

  public TestContextMultiSelectListPreference(Context context, AttributeSet attrs) {
    this(context, attrs, new TestAlarmDao());
  }

  TestContextMultiSelectListPreference(Context context, AttributeSet attrs, TestAlarmDao testAlarmDao) {
    super(context, attrs);
    Set<TestAlarmContext> testAlarmContexts = testAlarmDao.loadAllContexts();
    Set<TestAlarmContext> persistedEntries = SharedState.getSupervisedTestAlarms(context);
    Set<TestAlarmContext> filteredEntries = persistedEntries.stream().filter(testAlarmContexts::contains).collect(Collectors.toSet());
    if (!filteredEntries.containsAll(persistedEntries)) {
      SharedState.setSuperviseTestContexts(context, filteredEntries);
    }
    Set<CharSequence> validEntries = testAlarmContexts.stream().map(TestAlarmContext::getContext).collect(Collectors.toSet());
    setEntries(validEntries.toArray(new CharSequence[]{}));
    setEntryValues(validEntries.toArray(new CharSequence[]{}));
    setOnPreferenceChangeListener((preference, newValue) -> {
      String summary = newValue.toString();
      if (Set.class.isAssignableFrom(newValue.getClass())) {
        summary = toSummary((Set) newValue);
      }
      TestContextMultiSelectListPreference.this.setSummary(summary);
      return true;
    });
  }

  @Override
  public CharSequence getSummary() {
    String summary = super.getSummary().toString();
    if (summary.contains("%s")) {
      summary = toSummary(getValues());
    }
    return summary;
  }

  private String toSummary(Set<String> values) {
    String summary;
    summary = TextUtils.join(", ", values);
    if (summary.isEmpty()) {
      summary = getContext().getResources().getString(R.string.preferences_test_context_empty_selection);
    }
    return summary;
  }
}
