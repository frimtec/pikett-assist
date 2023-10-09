package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.CalendarDao.PROJECTION;
import static com.github.frimtec.android.pikettassist.service.dao.CalendarDao.SELECTION;
import static com.github.frimtec.android.pikettassist.service.dao.CalendarDao.SELECTION_ARGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import com.github.frimtec.android.pikettassist.domain.Calendar;

import org.junit.jupiter.api.Test;

import java.util.List;

class CalendarDaoTest {

  @Test
  void all() {
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToNext()).thenReturn(true, true, true, false);
    when(cursor.getInt(0)).thenReturn(11, 20, 25);
    when(cursor.getString(1)).thenReturn("cal1", "cal2", "cal3");
    ContentResolver resolver = mock(ContentResolver.class);
    when(resolver.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null))
        .thenReturn(cursor);
    Context context = mock(Context.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);

    CalendarDao dao = new CalendarDao(context);

    List<Calendar> calendars = dao.all();
    assertThat(calendars.size()).isEqualTo(3);
    assertCalendar(calendars.get(0), 11, "cal1");
    assertCalendar(calendars.get(1), 20, "cal2");
    assertCalendar(calendars.get(2), 25, "cal3");
  }

  @Test
  void allForNoCalendars() {
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToNext()).thenReturn(false);
    ContentResolver resolver = mock(ContentResolver.class);
    when(resolver.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null))
        .thenReturn(cursor);
    Context context = mock(Context.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    CalendarDao dao = new CalendarDao(context);

    List<Calendar> calendars = dao.all();
    assertThat(calendars).isEmpty();
  }

  @Test
  void allForNullCursorReturnsEmptyList() {
    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToNext()).thenReturn(false);
    ContentResolver resolver = mock(ContentResolver.class);
    when(resolver.query(CalendarContract.Calendars.CONTENT_URI, PROJECTION, SELECTION, SELECTION_ARGS, null))
        .thenReturn(null);
    Context context = mock(Context.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    CalendarDao dao = new CalendarDao(context);

    List<Calendar> calendars = dao.all();
    assertThat(calendars).isEmpty();
  }

  @Test
  void allForMissingPermissionReturnsEmptyList() {
    Context context = mock(Context.class);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(1);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);

    CalendarDao dao = new CalendarDao(context);

    List<Calendar> calendars = dao.all();
    assertThat(calendars).isEmpty();
    verifyNoMoreInteractions(resolver);
  }

  private void assertCalendar(Calendar calendar, int expectedId, String expectedName) {
    assertThat(calendar.id()).isEqualTo(expectedId);
    assertThat(calendar.name()).isEqualTo(expectedName);
  }
}