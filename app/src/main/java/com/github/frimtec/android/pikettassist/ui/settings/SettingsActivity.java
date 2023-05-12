package com.github.frimtec.android.pikettassist.ui.settings;

import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.CALENDAR_FILTER_ALL;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_BATTERY_WARN_LEVEL;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER;

import android.annotation.SuppressLint;
import android.app.LocaleManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.dao.CalendarDao;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;
import com.takisoft.preferencex.RingtonePreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    private static final int MAX_SUPPORTED_SIMS = 3;

    @SuppressLint("DefaultLocale")
    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);
      Context context = getContext();

      ContactPreference contactPreference = findPreference("alarm_operations_center_contact");
      if (contactPreference != null) {
        contactPreference.initLauncher(this);
      }

      RingtonePreference alarmRingTone = findPreference("alarm_ring_tone");
      if (alarmRingTone != null) {
        alarmRingTone.setSummaryProvider(
            (Preference.SummaryProvider<RingtonePreference>) preference ->
                preference.getRingtone() == null ? getString(R.string.preferences_alarm_ringtone_default) : preference.getRingtoneTitle());
      }

      EditTextPreference proPostTimeSeconds = findPreference("pre_post_run_time_seconds");
      if (proPostTimeSeconds != null) {
        proPostTimeSeconds.setSummaryProvider(
            (Preference.SummaryProvider<EditTextPreference>) preference -> {
              String value = preference.getText();
              return value + " " + getString("1".equals(value) ? R.string.units_second : R.string.units_seconds);
            });
      }

      SeekBarPreference batteryWarnLevel = findPreference(PREF_KEY_BATTERY_WARN_LEVEL);
      if (batteryWarnLevel != null) {
        batteryWarnLevel.setMin(10);
        batteryWarnLevel.setMax(50);
        batteryWarnLevel.setOnPreferenceChangeListener((preference, newValue) -> {
          ((SeekBarPreference) preference).setValue((int) newValue);
          return true;
        });
        batteryWarnLevel.setSummaryProvider(
            (Preference.SummaryProvider<SeekBarPreference>) preference -> String.format("%d%%", preference.getValue()));
      }

      SeekBarPreference lowSignalFilterTime = findPreference(PREF_KEY_LOW_SIGNAL_FILTER);
      if (lowSignalFilterTime != null) {
        lowSignalFilterTime.setMin(ApplicationPreferences.LOW_SIGNAL_FILTER_PREFERENCE.getMinIndex());
        lowSignalFilterTime.setMax(ApplicationPreferences.LOW_SIGNAL_FILTER_PREFERENCE.getMaxIndex());
        lowSignalFilterTime.setOnPreferenceChangeListener((preference, newValue) -> {
          ((SeekBarPreference) preference).setValue((int) newValue);
          return true;
        });
        lowSignalFilterTime.setSummaryProvider(
            (SeekBarPreference.SummaryProvider<SeekBarPreference>) preference -> {
              int value = preference.getValue();
              return value == 0 ? getString(R.string.state_off) : ApplicationPreferences.instance().convertLowSignalFilerToSeconds(value) + " " + getString(R.string.general_seconds);
            });
      }

      Preference testAlarmGroup = findPreference("test_alarm_group");
      if (testAlarmGroup != null) {
        testAlarmGroup.setSummaryProvider(
            (Preference.SummaryProvider<Preference>) preference ->
                enabledOrDisabled(ApplicationPreferences.instance().getTestAlarmEnabled(preference.getContext()))
        );
      }

      Preference dayNightProfileGroup = findPreference("day_night_profile_group");
      if (dayNightProfileGroup != null) {
        dayNightProfileGroup.setSummaryProvider(
            (Preference.SummaryProvider<Preference>) preference ->
                enabledOrDisabled(
                    ApplicationPreferences.instance().getManageVolumeEnabled(preference.getContext()) ||
                        ApplicationPreferences.instance().getBatterySaferAtNightEnabled(preference.getContext())

                )
        );

        ListPreference calendarSelection = findPreference("calendar_selection");
        if (calendarSelection != null) {
          List<CharSequence> entries = new ArrayList<>();
          List<CharSequence> entriesValues = new ArrayList<>();
          entries.add(getString(R.string.preferences_calendar_all));
          entriesValues.add(CALENDAR_FILTER_ALL);
          if (context != null) {
            new CalendarDao(context).all().forEach(calendar -> {
              entries.add(calendar.getName());
              entriesValues.add(String.valueOf(calendar.getId()));
            });
          }
          calendarSelection.setEntries(entries.toArray(new CharSequence[]{}));
          calendarSelection.setEntryValues(entriesValues.toArray(new CharSequence[]{}));
          calendarSelection.setDefaultValue(CALENDAR_FILTER_ALL);
        }

        ListPreference superviseSignalStrengthSubscription = findPreference("supervise_signal_strength_subscription");
        if (superviseSignalStrengthSubscription != null) {
          List<CharSequence> entries = new ArrayList<>();
          List<CharSequence> entriesValues = new ArrayList<>();

          if (context != null) {
            // Subscription are mostly counted starting from 1 therefor lets check for one more
            for (int i = 0; i < MAX_SUPPORTED_SIMS + 1; i++) {
              SignalStrengthService signalStrengthService = new SignalStrengthService(context, i);
              String networkOperatorName = signalStrengthService.getNetworkOperatorName();
              if (networkOperatorName != null) {
                entriesValues.add(String.valueOf(i));
                entries.add(String.format(Locale.getDefault(), "%s %d: %s", getString(R.string.subscription), i, networkOperatorName));
              } else {
                Log.d(TAG, "No phone manager for subscriptionId " + i);
              }
            }
          }
          superviseSignalStrengthSubscription.setEntries(entries.toArray(new CharSequence[]{}));
          superviseSignalStrengthSubscription.setEntryValues(entriesValues.toArray(new CharSequence[]{}));
          superviseSignalStrengthSubscription.setDefaultValue(getString(R.string.pref_default_supervise_signal_strength_subscription));
          if (entries.size() < 2) {
            superviseSignalStrengthSubscription.setVisible(false);
          }
          if (entries.size() == 1) {
            int subscriptionId = Integer.parseInt(String.valueOf(entriesValues.get(0)));
            ApplicationPreferences.instance().setSuperviseSignalStrengthSubscription(
                context,
                subscriptionId
            );
            Log.d(TAG, "Setting rest to one and only subscriptionId: " + subscriptionId);
          }
        }
      }
      if (Build.VERSION.SDK_INT >= 33) {
        ListPreference appLanguage = findPreference("app_language");
        if (appLanguage != null) {
          appLanguage.setVisible(true);
          LocaleManager localeManager = context.getSystemService(LocaleManager.class);
          String currentAppLocales = localeManager.getApplicationLocales().toLanguageTags();
          appLanguage.setValue(currentAppLocales.split("-")[0]);

          appLanguage.setOnPreferenceChangeListener(
              (preference, newValue) -> {
                localeManager.setApplicationLocales(
                    LocaleList.forLanguageTags(newValue.toString())
                );
                return true;
              });
        }
      }

      ListPreference appTheme = findPreference("app_theme");
      if (appTheme != null) {
        appTheme.setOnPreferenceChangeListener(
            (preference, newValue) -> {
              AppCompatDelegate.setDefaultNightMode(Integer.parseInt(String.valueOf(newValue)));
              return true;
            });
      }
    }


    private String enabledOrDisabled(boolean flag) {
      return getString(flag ? R.string.general_enabled : R.string.general_disabled);
    }
  }
}