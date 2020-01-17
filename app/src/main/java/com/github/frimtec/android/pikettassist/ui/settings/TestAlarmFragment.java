package com.github.frimtec.android.pikettassist.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import java.util.Set;
import java.util.stream.Collectors;

public class TestAlarmFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.test_alarm_preferences, rootKey);

    MultiSelectListPreference countingPreference = findPreference("test_alarm_check_weekdays");
    if (countingPreference != null) {
      countingPreference.setSummaryProvider(
          (Preference.SummaryProvider<MultiSelectListPreference>) preference -> weekDaysValues(preference, preference.getValues()));
    }

    RingtonePreference testAlarmRingTone = findPreference("test_alarm_ring_tone");
    if (testAlarmRingTone != null) {
      testAlarmRingTone.setSummaryProvider(
          (Preference.SummaryProvider<RingtonePreference>) preference ->
              preference.getRingtone() == null ? preference.getContext().getResources().getString(R.string.preferences_alarm_ringtone_default) : preference.getRingtoneTitle());
    }


    MultiSelectListPreference superviseTestContexts = findPreference("supervise_test_contexts");
    if (superviseTestContexts != null) {
      TestAlarmDao testAlarmDao = new TestAlarmDao();
      Set<TestAlarmContext> testAlarmContexts = testAlarmDao.loadAllContexts();
      Set<TestAlarmContext> persistedEntries = SharedState.getSupervisedTestAlarms(getContext());
      Set<TestAlarmContext> filteredEntries = persistedEntries.stream().filter(testAlarmContexts::contains).collect(Collectors.toSet());
      if (!filteredEntries.containsAll(persistedEntries)) {
        SharedState.setSuperviseTestContexts(getContext(), filteredEntries);
      }
      Set<CharSequence> validEntries = testAlarmContexts.stream().map(TestAlarmContext::getContext).collect(Collectors.toSet());
      superviseTestContexts.setEntries(validEntries.toArray(new CharSequence[]{}));
      superviseTestContexts.setEntryValues(validEntries.toArray(new CharSequence[]{}));

      superviseTestContexts.setSummaryProvider(
          (Preference.SummaryProvider<MultiSelectListPreference>) preference ->
              TextUtils.join(", ", preference.getValues()));
    }
  }

  private static String weekDaysValues(Preference preference, Set<String> values) {
    String[] weekdays = preference.getContext().getResources().getStringArray(R.array.weekdays);
    return values.stream().map(id -> weekdays[Integer.parseInt(id) - 1]).collect(Collectors.joining(", "));
  }

}
