package com.github.frimtec.android.pikettassist.settings;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

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
  }
}
