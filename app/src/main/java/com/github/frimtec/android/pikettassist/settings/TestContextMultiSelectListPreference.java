package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import java.util.*;

public class TestContextMultiSelectListPreference extends MultiSelectListPreference {
  public TestContextMultiSelectListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    List<CharSequence> entries = new ArrayList<>();

    entries.add("general");
    entries.add("hossa");
    entries.add("CHF_SNB");
    entries.add("EUR_SECB");

    setEntries(entries.toArray(new CharSequence[]{}));
    setEntryValues(entries.toArray(new CharSequence[]{}));

    setOnPreferenceChangeListener((preference, newValue) -> {
      String summary = newValue.toString();
      if(Collection.class.isAssignableFrom(newValue.getClass())) {
        summary = String.join(", ", ((Collection) newValue));
      }
      TestContextMultiSelectListPreference.this.setSummary(summary);
      return true;
    });
  }

  @Override
  public CharSequence getSummary() {
    String summary = super.getSummary().toString();
    if (summary.contains("%s")) {
      summary = String.join(", ", getValues());
    }
    return summary;
  }
}
