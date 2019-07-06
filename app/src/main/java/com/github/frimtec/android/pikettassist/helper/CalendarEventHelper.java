package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.provider.CalendarContract;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.PikettShift;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public final class CalendarEventHelper {

  private static final String TAG = "CalendarEventHelper";


  private CalendarEventHelper() {
  }

  public static boolean hasPikettEventForNow(Context context, String eventTitleFilterPattern) {
    return getPikettShifts(context, eventTitleFilterPattern).stream()
        .anyMatch(PikettShift::isNow);
  }

  public static List<PikettShift> getPikettShifts(Context context, String eventTitleFilterPattern) {
    String[] projection = new String[]{
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND
    };
    Calendar startTime = Calendar.getInstance();
    startTime.add(Calendar.DATE, -30);
    Calendar endTime = Calendar.getInstance();

    endTime.add(Calendar.DATE, 180);
    String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTEND + " <= " + endTime.getTimeInMillis() + " ) AND ( deleted != 1 ))";
    List<PikettShift> events = new LinkedList<>();
    try (@SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, null, null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String eventTitle = cursor.getString(1);
          Instant eventStartTime = Instant.ofEpochMilli(cursor.getLong(2));
          Instant eventEndTime = Instant.ofEpochMilli(cursor.getLong(3));
          Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
          if (eventTitle.matches(eventTitleFilterPattern)) {
            events.add(new PikettShift(id, eventTitle, eventStartTime, eventEndTime));
          }
        } while (cursor.moveToNext());
      }
    }
    events.sort(Comparator.comparing(shift -> shift.getStartTime(false)));
    return events;
  }
}
