package com.github.frimtec.android.pikettassist.settings;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.preference.ListPreference;
import android.provider.CalendarContract;
import android.util.AttributeSet;

import com.github.frimtec.android.pikettassist.R;

import java.util.ArrayList;
import java.util.List;

import static com.github.frimtec.android.pikettassist.helper.Feature.PERMISSION_CALENDAR_READ;
import static com.github.frimtec.android.pikettassist.state.SharedState.CALENDAR_FILTER_ALL;

public class CalendarListPreference extends ListPreference {

  private final static String[] PROJECTION = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
  private final static String SELECTION = "(" + CalendarContract.Calendars.VISIBLE + " = ?)";
  private final static String[] SELECTION_ARGS = new String[]{"1"};

  @SuppressLint("MissingPermission")
  public CalendarListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    List<CharSequence> entries = new ArrayList<>();
    List<CharSequence> entriesValues = new ArrayList<>();

    entries.add(context.getResources().getString(R.string.preferences_calendar_all));
    entriesValues.add(CALENDAR_FILTER_ALL);

    if(PERMISSION_CALENDAR_READ.isAllowed(getContext())) {
      ContentResolver cr = context.getContentResolver();
      try (Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null)) {
        while (cursor != null && cursor.moveToNext()) {
          entries.add(cursor.getString(1));
          entriesValues.add(String.valueOf(cursor.getInt(0)));
        }
      }
    }
    setEntries(entries.toArray(new CharSequence[]{}));
    setEntryValues(entriesValues.toArray(new CharSequence[]{}));
    setDefaultValue(CALENDAR_FILTER_ALL);
  }
}