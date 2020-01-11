package com.github.frimtec.android.pikettassist.ui.preferences;

import android.content.Context;
import android.net.Uri;
import android.preference.RingtonePreference;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;

class AlarmRingtonePreference extends RingtonePreference {

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
    return (preferenceValue != null && preferenceValue.isEmpty()) ? getContext().getResources().getString(R.string.preferences_alarm_ringtone_default) : extractTitle(preferenceValue);
  }

  private String extractTitle(String preferenceValue) {
    String title = Uri.parse(preferenceValue).getQueryParameter("title");
    return title != null ? title : preferenceValue.substring(preferenceValue.lastIndexOf("/") + 1);
  }
}
