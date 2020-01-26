package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.dao.CalendarDao;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import java.util.ArrayList;
import java.util.List;

import static com.github.frimtec.android.pikettassist.state.SharedState.CALENDAR_FILTER_ALL;
import static com.github.frimtec.android.pikettassist.state.SharedState.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;


public class SettingsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings, new SettingsFragment())
        .commit();
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  public static class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private static final int MAX_SUPPORTED_SIMS = 2;

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);

      RingtonePreference alarmRingTone = findPreference("alarm_ring_tone");
      if (alarmRingTone != null) {
        alarmRingTone.setSummaryProvider(
            (Preference.SummaryProvider<RingtonePreference>) preference ->
                preference.getRingtone() == null ? preference.getContext().getResources().getString(R.string.preferences_alarm_ringtone_default) : preference.getRingtoneTitle());
      }

      SeekBarPreference lowSignalFilterTime = findPreference("low_signal_filter");
      if (lowSignalFilterTime != null) {
        lowSignalFilterTime.setOnPreferenceChangeListener((preference, newValue) -> {
          ((SeekBarPreference) preference).setValue((int) newValue);
          return true;
        });
        lowSignalFilterTime.setSummaryProvider(
            (SeekBarPreference.SummaryProvider<SeekBarPreference>) preference -> {
              int value = preference.getValue();
              Resources resources = preference.getContext().getResources();
              return value == 0 ? resources.getString(R.string.state_off) : (value * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR) + " " + resources.getString(R.string.general_seconds);
            });
      }

      Preference testAlarmGroup = findPreference("test_alarm_group");
      if (testAlarmGroup != null) {
        testAlarmGroup.setSummaryProvider(
            (Preference.SummaryProvider<Preference>) preference ->
                enabledOrDisabled(preference.getContext(), SharedState.getTestAlarmEnabled(preference.getContext()))
        );
      }

      Preference dayNightProfileGroup = findPreference("day_night_profile_group");
      if (dayNightProfileGroup != null) {
        dayNightProfileGroup.setSummaryProvider(
            (Preference.SummaryProvider<Preference>) preference ->
                enabledOrDisabled(preference.getContext(), SharedState.getManageVolumeEnabled(preference.getContext()))
        );

        ListPreference calendarSelection = findPreference("calendar_selection");
        if (calendarSelection != null) {
          List<CharSequence> entries = new ArrayList<>();
          List<CharSequence> entriesValues = new ArrayList<>();
          entries.add(getContext().getResources().getString(R.string.preferences_calendar_all));
          entriesValues.add(CALENDAR_FILTER_ALL);
          new CalendarDao(getContext()).all().forEach(calendar -> {
            entries.add(calendar.getName());
            entriesValues.add(String.valueOf(calendar.getId()));
          });
          calendarSelection.setEntries(entries.toArray(new CharSequence[]{}));
          calendarSelection.setEntryValues(entriesValues.toArray(new CharSequence[]{}));
          calendarSelection.setDefaultValue(CALENDAR_FILTER_ALL);
        }

        ListPreference superviseSignalStrengthSubscription = findPreference("supervise_signal_strength_subscription");
        if (superviseSignalStrengthSubscription != null) {
          List<CharSequence> entries = new ArrayList<>();
          List<CharSequence> entriesValues = new ArrayList<>();

          for (int i = 0; i < MAX_SUPPORTED_SIMS; i++) {
            SignalStrengthService signalStrengthService = new SignalStrengthService(getContext(), i);
            String networkOperatorName = signalStrengthService.getNetworkOperatorName();
            if (networkOperatorName != null) {
              entriesValues.add(String.valueOf(i));
              entries.add(String.format("%s %s: %s", getContext().getString(R.string.subscription), String.valueOf(i + 1), networkOperatorName));
            } else {
              Log.d(TAG, "No phone manager for subscriptionId " + i);
            }
          }
          superviseSignalStrengthSubscription.setEntries(entries.toArray(new CharSequence[]{}));
          superviseSignalStrengthSubscription.setEntryValues(entriesValues.toArray(new CharSequence[]{}));
          superviseSignalStrengthSubscription.setDefaultValue(getContext().getString(R.string.pref_default_supervise_signal_strength_subscription));
          if (entries.size() < 2) {
            superviseSignalStrengthSubscription.setVisible(false);
          }
        }
      }
    }

    private static String enabledOrDisabled(Context context, boolean flag) {
      return context.getResources().getString(flag ? R.string.general_enabled : R.string.general_disabled);
    }
  }
}