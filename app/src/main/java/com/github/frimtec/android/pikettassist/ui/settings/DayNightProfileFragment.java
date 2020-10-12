package com.github.frimtec.android.pikettassist.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.SwitchPreference;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_BATTERY_SAFER_AT_NIGHT;

public class DayNightProfileFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.day_night_preferences, rootKey);

    SwitchPreference enableBatterySaferAtNight = findPreference(PREF_KEY_BATTERY_SAFER_AT_NIGHT);
    if (enableBatterySaferAtNight != null) {
      enableBatterySaferAtNight.setSummary(getBatterySaferAtNightSummary(ApplicationPreferences.getBatterySaferAtNightEnabled(enableBatterySaferAtNight.getContext())));
      enableBatterySaferAtNight.setOnPreferenceChangeListener((preference, newValue) -> {
        preference.setSummary(getBatterySaferAtNightSummary(Boolean.TRUE.equals(newValue)));
        return true;
      });
    }
  }

  private static int getBatterySaferAtNightSummary(boolean enabled) {
    return enabled ? R.string.pref_summary_battery_safer_at_night_enabled : R.string.pref_summary_battery_safer_at_night_disabled;
  }
}
