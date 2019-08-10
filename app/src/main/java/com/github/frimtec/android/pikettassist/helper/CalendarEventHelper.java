package com.github.frimtec.android.pikettassist.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.provider.CalendarContract;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.PikettShift;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static com.github.frimtec.android.pikettassist.state.SharedState.CALENDAR_FILTER_ALL;

public final class CalendarEventHelper {

  private static final String TAG = "CalendarEventHelper";


  private CalendarEventHelper() {
  }

  public static boolean hasPikettEventForNow(Context context, String eventTitleFilterPattern, String calendarSelection) {
    return getPikettShifts(context, eventTitleFilterPattern, calendarSelection).stream()
        .anyMatch(PikettShift::isNow);
  }

  public static List<PikettShift> getPikettShifts(Context context, String eventTitleFilterPattern, String calendarSelection) {
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
    Log.d(TAG, "calendarSelection: " + calendarSelection);
    String selection = "( " + CalendarContract.Events.DTSTART + " >= " + startTime.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTEND + " <= " + endTime.getTimeInMillis() + " ) AND ( deleted != 1 )";
    String[] args = new String[0];
    if (!CALENDAR_FILTER_ALL.equals(calendarSelection)) {
      selection = selection + " AND (" + CalendarContract.Events.CALENDAR_ID + " = ?)";
      args = new String[]{calendarSelection};
    }
    Log.d(TAG, "selection: " + selection);
    List<PikettShift> events = new LinkedList<>();
    try (@SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, projection, selection, args, null)) {
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
