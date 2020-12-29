package com.github.frimtec.android.pikettassist.service.dao;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import com.github.frimtec.android.pikettassist.domain.Calendar;

import java.util.ArrayList;
import java.util.List;

import static com.github.frimtec.android.pikettassist.service.system.Feature.PERMISSION_CALENDAR_READ;

public class CalendarDao {

  static final String[] PROJECTION = new String[]{
      CalendarContract.Calendars._ID,
      CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
  };
  static final String SELECTION = "(" + CalendarContract.Calendars.VISIBLE + " = ?)";
  static final String[] SELECTION_ARGS = new String[]{"1"};

  private final Context context;
  private final ContentResolver contentResolver;

  public CalendarDao(Context context) {
    this.context = context;
    this.contentResolver = context.getContentResolver();
  }

  public List<Calendar> all() {
    List<Calendar> allCalendars = new ArrayList<>();
    if (PERMISSION_CALENDAR_READ.isAllowed(this.context)) {
      try (@SuppressLint("MissingPermission") Cursor cursor = this.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null)) {
        while (cursor != null && cursor.moveToNext()) {
          allCalendars.add(new Calendar(cursor.getInt(0), cursor.getString(1)));
        }
      }
    }
    return allCalendars;
  }

}
