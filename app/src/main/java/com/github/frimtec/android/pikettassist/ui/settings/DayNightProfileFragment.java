package com.github.frimtec.android.pikettassist.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.takisoft.preferencex.PreferenceFragmentCompat;

import com.github.frimtec.android.pikettassist.R;

public class DayNightProfileFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.day_night_preferences, rootKey);
  }
}
