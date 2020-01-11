package com.github.frimtec.android.pikettassist.ui.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.dao.CalendarDao;

import java.util.ArrayList;
import java.util.List;

import static com.github.frimtec.android.pikettassist.state.SharedState.CALENDAR_FILTER_ALL;

class CalendarListPreference extends ListPreference {

  @SuppressLint("MissingPermission")
  public CalendarListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    List<CharSequence> entries = new ArrayList<>();
    List<CharSequence> entriesValues = new ArrayList<>();
    entries.add(context.getResources().getString(R.string.preferences_calendar_all));
    entriesValues.add(CALENDAR_FILTER_ALL);
    new CalendarDao(context).all().forEach(calendar -> {
      entries.add(calendar.getName());
      entriesValues.add(String.valueOf(calendar.getId()));
    });
    setEntries(entries.toArray(new CharSequence[]{}));
    setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    setDefaultValue(CALENDAR_FILTER_ALL);
  }
}