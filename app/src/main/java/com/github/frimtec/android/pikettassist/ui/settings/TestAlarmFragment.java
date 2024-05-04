package com.github.frimtec.android.pikettassist.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAlarmFragment extends PreferenceFragmentCompat {

  /**
   * @noinspection SimplifyStreamApiCallChains
   */
  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.test_alarm_preferences, rootKey);

    EditTextPreference testAlarmAcceptTimeWindowMinutes = findPreference("test_alarm_accept_time_window_minutes");
    if (testAlarmAcceptTimeWindowMinutes != null) {
      testAlarmAcceptTimeWindowMinutes.setSummaryProvider(
          (Preference.SummaryProvider<EditTextPreference>) preference -> {
            String value = preference.getText();
            return value + " " + getString("1".equals(value) ? R.string.units_minute : R.string.units_minutes);
          });
    }

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
      Map<TestAlarmContext, TestAlarm> contextToTestAlarms = testAlarmDao.loadAll().stream().collect(Collectors.toMap(
          TestAlarm::context,
          testAlarm -> testAlarm
      ));
      Set<TestAlarmContext> persistedEntries = ApplicationPreferences.instance().getSupervisedTestAlarms(getContext());
      Set<TestAlarmContext> filteredEntries = persistedEntries.stream().filter(contextToTestAlarms::containsKey).collect(Collectors.toSet());
      if (!filteredEntries.containsAll(persistedEntries)) {
        ApplicationPreferences.instance().setSuperviseTestContexts(getContext(), filteredEntries);
      }

      record Entry(CharSequence key, CharSequence value) {

      }

      List<Entry> entries = contextToTestAlarms.values().stream()
          .sorted(Comparator.comparing(TestAlarm::name))
          .map(testAlarm -> new Entry(
              testAlarm.context().context(),
              testAlarm.name()
          )).collect(Collectors.toList());
      superviseTestContexts.setEntries(
          entries.stream().map(Entry::value).collect(Collectors.toList()).toArray(new CharSequence[]{})
      );
      superviseTestContexts.setEntryValues(
          entries.stream().map(Entry::key).collect(Collectors.toList()).toArray(new CharSequence[]{})
      );
      superviseTestContexts.setSummaryProvider(
          (Preference.SummaryProvider<MultiSelectListPreference>) preference ->
              TextUtils.join(", ", preference.getValues().stream().map(TestAlarmContext::new).map(context -> {
                TestAlarm testAlarm = contextToTestAlarms.get(context);
                return testAlarm != null ? testAlarm.name() : context.context();
              }).sorted().collect(Collectors.toList())));
    }
  }

  private static String weekDaysValues(Preference preference, Set<String> values) {
    String[] weekdays = preference.getContext().getResources().getStringArray(R.array.weekdays);
    return values.stream().map(id -> weekdays[Integer.parseInt(id) - 1]).collect(Collectors.joining(", "));
  }

}
