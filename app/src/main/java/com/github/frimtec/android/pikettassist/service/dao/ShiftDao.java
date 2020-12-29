package com.github.frimtec.android.pikettassist.service.dao;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Shift;

import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.CALENDAR_FILTER_ALL;

public final class ShiftDao {

  private static final String TAG = "ShiftDao";

  private final Context context;
  private final ContentResolver contentResolver;

  static final String[] PROJECTION = new String[]{
      CalendarContract.Instances.EVENT_ID,
      CalendarContract.Instances.TITLE,
      CalendarContract.Instances.BEGIN,
      CalendarContract.Instances.END,
      CalendarContract.Instances.CALENDAR_ID,
      CalendarContract.Instances.SELF_ATTENDEE_STATUS
  };

  private final BiFunction<Instant, Instant, Uri> eventUriProvider;

  public ShiftDao(Context context) {
    this(context, ShiftDao::buildEventsUri);
  }

  ShiftDao(Context context, BiFunction<Instant, Instant, Uri> eventUriProvider) {
    this.context = context;
    this.contentResolver = context.getContentResolver();
    this.eventUriProvider = eventUriProvider;
  }

  public List<Shift> getShifts(String eventTitleFilterPattern, String calendarSelection) {
    if (!PERMISSION_CALENDAR_READ.isAllowed(context)) {
      Log.e(TAG, "No permissions to read calendar");
      return Collections.emptyList();
    }
    Instant now = Instant.now();
    Instant startTime = now.minus(30, ChronoUnit.DAYS);
    Instant endTime = now.plus(180, ChronoUnit.DAYS);
    String selection = "deleted != 1";
    String[] args = new String[0];
    if (!CALENDAR_FILTER_ALL.equals(calendarSelection)) {
      selection = selection + " AND " + CalendarContract.Instances.CALENDAR_ID + " = ?";
      args = new String[]{calendarSelection};
    }
    List<Shift> events = new LinkedList<>();
    Uri eventsUri = eventUriProvider.apply(startTime, endTime);
    try (@SuppressLint("MissingPermission") Cursor cursor = this.contentResolver.query(eventsUri, PROJECTION, selection, args, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        do {
          long id = cursor.getLong(0);
          String eventTitle = cursor.getString(1);
          Instant eventStartTime = Instant.ofEpochMilli(cursor.getLong(2));
          Instant eventEndTime = Instant.ofEpochMilli(cursor.getLong(3));
          int selfAttendeeStatus = cursor.getInt(5);

          Pattern pattern = Pattern.compile(eventTitleFilterPattern, Pattern.CASE_INSENSITIVE);
          if (pattern.matcher(nonNullString(eventTitle)).matches()) {
            events.add(new Shift(id, eventTitle, eventStartTime, eventEndTime, isConfirmed(selfAttendeeStatus)));
          }
        } while (cursor.moveToNext());
      }
    }
    events.sort(Comparator.comparing(Shift::getStartTime));
    return events;
  }

  private boolean isConfirmed(int selfAttendeeStatus) {
    return selfAttendeeStatus == Attendees.ATTENDEE_STATUS_ACCEPTED ||
        selfAttendeeStatus == Attendees.ATTENDEE_STATUS_NONE;
  }

  private static CharSequence nonNullString(String value) {
    return value != null ? value : "";
  }

  private static Uri buildEventsUri(Instant startTime, Instant endTime) {
    Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
    ContentUris.appendId(eventsUriBuilder, startTime.toEpochMilli());
    ContentUris.appendId(eventsUriBuilder, endTime.toEpochMilli());
    return eventsUriBuilder.build();
  }
}
