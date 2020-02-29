package com.github.frimtec.android.pikettassist.service.dao;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.provider.CalendarContract;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Shift;

import org.threeten.bp.Instant;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.CALENDAR_FILTER_ALL;

public final class ShiftDao {

  private static final String TAG = "ShiftDao";

  private final Context context;
  private final ContentResolver contentResolver;

  public ShiftDao(Context context) {
    this.context = context;
    this.contentResolver = context.getContentResolver();
  }

  public List<Shift> getShifts(String eventTitleFilterPattern, String calendarSelection) {
    String[] projection = new String[]{
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.CALENDAR_ID
    };
    Calendar startTime = Calendar.getInstance();
    startTime.add(Calendar.DATE, -30);
    Calendar endTime = Calendar.getInstance();

    endTime.add(Calendar.DATE, 180);
    String selection = "( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTEND + " <= " + endTime.getTimeInMillis() + " ) AND ( deleted != 1 )";
    String[] args = new String[0];
    if (!CALENDAR_FILTER_ALL.equals(calendarSelection)) {
      selection = selection + " AND (" + CalendarContract.Events.CALENDAR_ID + " = ?)";
      args = new String[]{calendarSelection};
    }
    if(!PERMISSION_CALENDAR_READ.isAllowed(context)) {
      Log.e(TAG, "No permissions to read calendar");
      return Collections.emptyList();
    }
    List<Shift> events = new LinkedList<>();
    try (@SuppressLint("MissingPermission") Cursor cursor = this.contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, args, null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String eventTitle = cursor.getString(1);
          Instant eventStartTime = Instant.ofEpochMilli(cursor.getLong(2));
          Instant eventEndTime = Instant.ofEpochMilli(cursor.getLong(3));

          Pattern pattern = Pattern.compile(eventTitleFilterPattern, Pattern.CASE_INSENSITIVE);
          if (pattern.matcher(nonNullString(eventTitle)).matches()) {
            events.add(new Shift(id, eventTitle, eventStartTime, eventEndTime));
          }
        } while (cursor.moveToNext());
      }
    }
    events.sort(Comparator.comparing(Shift::getStartTime));
    return events;
  }

  private static CharSequence nonNullString(String value) {
    return value != null ? value : "";
  }
}
