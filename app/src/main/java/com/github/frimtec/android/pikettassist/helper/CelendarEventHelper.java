package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.provider.CalendarContract;
import android.util.Log;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class CelendarEventHelper {

  private static final String TAG = "CelendarEventHelper";

  private static final Duration TIME_TOLLERANCE = Duration.ofMinutes(5);

  private CelendarEventHelper() {
  }

  public static boolean hasPikettEventForNow(Context context, String eventTitleFilterPattern) {
    String[] projection = new String[]{
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND
    };
    Calendar startTime = Calendar.getInstance();
    startTime.add(Calendar.DATE, -10);
    Calendar endTime = Calendar.getInstance();

    endTime.add(Calendar.DATE, 10);
    String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + endTime.getTimeInMillis() + " ) AND ( deleted != 1 ))";
    List<String> events = new ArrayList<>();
    try (@SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          String eventTitle = cursor.getString(1);
          Instant eventStartTime = Instant.ofEpochMilli(cursor.getLong(2));
          Instant eventEndTime = Instant.ofEpochMilli(cursor.getLong(3));
          Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
          if (eventTitle.matches(eventTitleFilterPattern) && now.isAfter(eventStartTime.minus(TIME_TOLLERANCE)) && now.isBefore(eventEndTime.plus(TIME_TOLLERANCE))) {
            Log.d(TAG, String.format("Matching event: %s [%s - %s]", eventTitle, eventStartTime, eventEndTime));
            return true;
          }
          Log.d(TAG, String.format("Not matching event: %s [%s - %s]", eventTitle, eventStartTime, eventEndTime));
        } while (cursor.moveToNext());
      }
    }
    return false;
  }

}
