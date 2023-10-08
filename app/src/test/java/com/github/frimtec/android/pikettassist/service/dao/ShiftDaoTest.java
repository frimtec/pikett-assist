package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.ShiftDao.PROJECTION;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.CALENDAR_FILTER_ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.github.frimtec.android.pikettassist.domain.Shift;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

class ShiftDaoTest {

  @SuppressWarnings("unchecked")
  private static final BiFunction<Instant, Instant, Uri> URI_PROVIDER = mock(BiFunction.class);

  @Test
  void getShiftsForAllCalendars() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Shift shift0 = new Shift(0, "PIKETT-0", now.minus(5, ChronoUnit.DAYS), now.minus(0, ChronoUnit.DAYS), true, Collections.emptyList());
    Shift shift1 = new Shift(20, "Pikett-1", now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), true, Collections.emptyList());
    Shift shift2 = new Shift(14, "Any pikett-2", now.plus(2, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS), false, Collections.emptyList());
    Shift shift3 = new Shift(15, "piket-3", now.plus(2, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS), false, Collections.emptyList());
    Shift shift4 = new Shift(16, null, now.plus(2, ChronoUnit.DAYS), now.plus(3, ChronoUnit.DAYS), false, Collections.emptyList());
    Cursor cursor = createShiftCursor(Arrays.asList(
        shift0,
        shift2,
        shift3, // not matching title
        shift1,
        shift4  // null title
    ), "");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1"), eq(new String[0]), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", CALENDAR_FILTER_ALL, null);
    assertThat(shifts.size()).isEqualTo(3);
    assertShift(shifts.get(0), shift0);
    assertShift(shifts.get(1), shift1);
    assertShift(shifts.get(2), shift2);
  }

  @Test
  void getShiftsForSpecificCalendar() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Shift shift1 = new Shift(20, "Pikett-1", now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), true, Collections.emptyList());
    Cursor cursor = createShiftCursor(Collections.singletonList(shift1), "");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1 AND calendar_id = ?"), eq(new String[]{"frimtec"}), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", "frimtec", null);
    assertThat(shifts.size()).isEqualTo(1);
    assertShift(shifts.get(0), shift1);
  }

  @Test
  void getShiftsForNoPartnerContacts() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Shift shift1 = new Shift(20, "Pikett-1", now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), true, Collections.emptyList());
    Cursor cursor = createShiftCursor(Collections.singletonList(shift1), "Note - Deputy: one Deputy: one\nDeputy: two");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1 AND calendar_id = ?"), eq(new String[]{"frimtec"}), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", "frimtec", "");
    assertThat(shifts.size()).isEqualTo(1);
    assertShift(shifts.get(0), shift1);
  }

  @Test
  void getShiftsForWithPartnerContacts() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Shift shift1 = new Shift(20, "Pikett-1", now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), true, Arrays.asList("one", "two"));
    Cursor cursor = createShiftCursor(Collections.singletonList(shift1), "Note - Deputy: one Deputy: one\nDeputy: two");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1 AND calendar_id = ?"), eq(new String[]{"frimtec"}), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", "frimtec", "Deputy:\\s([a-z]{3})(\\s|$)");
    assertThat(shifts.size()).isEqualTo(1);
    assertShift(shifts.get(0), shift1);
  }

  @Test
  void getShiftsForWithBadPartnerContactsPattern() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Shift shift1 = new Shift(20, "Pikett-1", now.minus(3, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), true, Collections.emptySet());
    Cursor cursor = createShiftCursor(Collections.singletonList(shift1), "Note - Deputy: one Deputy: one\nDeputy: two");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1 AND calendar_id = ?"), eq(new String[]{"frimtec"}), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", "frimtec", "BAD_PATTERN(");
    assertThat(shifts.size()).isEqualTo(1);
    assertShift(shifts.get(0), shift1);
  }

  @Test
  void getShiftsForMissingPermissionReturnsEmptyList() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(1);
    ShiftDao dao = new ShiftDao(context);

    List<Shift> shifts = dao.getShifts(".*pikett.*", CALENDAR_FILTER_ALL, null);
    assertThat(shifts).isEmpty();
    verifyNoMoreInteractions(resolver);
  }

  @Test
  void getShiftsForNullCursorReturnsEmptyList() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1"), eq(new String[0]), isNull()))
        .thenReturn(null);

    List<Shift> shifts = dao.getShifts(".*pikett.*", CALENDAR_FILTER_ALL, null);
    assertThat(shifts).isEmpty();
  }

  @Test
  void getShiftsForEmptyCursorReturnsEmptyList() {
    Context context = mock(Context.class);
    ContentResolver resolver = mock(ContentResolver.class);
    when(context.getContentResolver()).thenReturn(resolver);
    when(context.checkPermission("android.permission.READ_CALENDAR", 0, 0))
        .thenReturn(0);
    ShiftDao dao = new ShiftDao(context, URI_PROVIDER);
    Cursor cursor = createShiftCursor(Collections.emptyList(), "");
    when(resolver.query(any(), eq(PROJECTION), eq("deleted != 1"), eq(new String[0]), isNull()))
        .thenReturn(cursor);

    List<Shift> shifts = dao.getShifts(".*pikett.*", CALENDAR_FILTER_ALL, null);
    assertThat(shifts).isEmpty();
  }

  private void assertShift(Shift shift, Shift expectedShift) {
    assertThat(shift.getId()).isEqualTo(expectedShift.getId());
    assertThat(shift.getTitle()).isEqualTo(expectedShift.getTitle());
    assertThat(shift.getStartTime()).isEqualTo(expectedShift.getStartTime());
    assertThat(shift.getEndTime()).isEqualTo(expectedShift.getEndTime());
    assertThat(shift.isConfirmed()).isEqualTo(expectedShift.isConfirmed());
    assertThat(shift.getPartners()).isEqualTo(expectedShift.getPartners());
  }

  private Cursor createShiftCursor(List<Shift> shifts, String notes) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(shifts.size());
    when(cursor.moveToFirst()).thenReturn(!shifts.isEmpty());
    if (!shifts.isEmpty()) {
      Boolean[] moveResults = new Boolean[shifts.size() - 1];
      Arrays.fill(moveResults, true);
      if (moveResults.length > 1) {
        moveResults[moveResults.length - 1] = false;
      }
      List<Long> ids = shifts.stream().map(Shift::getId).toList();
      List<String> titles = shifts.stream().map(Shift::getTitle).toList();
      List<Long> startTimes = shifts.stream().map(Shift::getStartTime).map(Instant::toEpochMilli).toList();
      List<Long> endTimes = shifts.stream().map(Shift::getEndTime).map(Instant::toEpochMilli).toList();

      AtomicInteger index = new AtomicInteger(0);
      int[] confirmedStates = new int[]{0, 1};
      List<Integer> attendeeStatus = shifts.stream().map(Shift::isConfirmed).map(confirmed -> confirmed ? confirmedStates[index.getAndIncrement() % 2] : 4).toList();

      when(cursor.moveToNext()).thenReturn(shifts.size() > 1, moveResults);
      when(cursor.getLong(0)).thenReturn(ids.get(0), ids.subList(1, ids.size()).toArray(new Long[0]));
      when(cursor.getString(1)).thenReturn(titles.get(0), titles.subList(1, titles.size()).toArray(new String[0]));
      when(cursor.getLong(2)).thenReturn(startTimes.get(0), startTimes.subList(1, startTimes.size()).toArray(new Long[0]));
      when(cursor.getLong(3)).thenReturn(endTimes.get(0), endTimes.subList(1, endTimes.size()).toArray(new Long[0]));
      when(cursor.getInt(5)).thenReturn(attendeeStatus.get(0), attendeeStatus.subList(1, attendeeStatus.size()).toArray(new Integer[0]));
      when(cursor.getString(6)).thenReturn(notes);
    }
    return cursor;
  }

}