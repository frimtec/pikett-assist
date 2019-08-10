package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;

import java.util.Collection;

public class AlarmRingtonePreference extends RingtonePreference {

  public AlarmRingtonePreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOnPreferenceChangeListener((preference, newValue) -> {
      String summary = getValue(newValue.toString());
      AlarmRingtonePreference.this.setSummary(summary);
      return true;
    });
  }


  @Override
  public CharSequence getSummary() {
    String summary = super.getSummary().toString();
    if (summary.contains("%s")) {
      summary = getValue(getPersistedString(""));
    }
    return summary;
  }

  private String getValue(String preferenceValue) {
    return (preferenceValue != null && preferenceValue.isEmpty()) ? "Default ringtone" : extractTitle(preferenceValue);
  }

  private String extractTitle(String preferenceValue) {
    return Uri.parse(preferenceValue).getQueryParameter("title");
  }
}
