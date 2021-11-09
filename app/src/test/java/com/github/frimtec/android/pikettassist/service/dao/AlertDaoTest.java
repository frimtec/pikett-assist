package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.AlertDao.ALERT_COLUMNS;
import static com.github.frimtec.android.pikettassist.service.dao.AlertDao.CALL_COLUMNS;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_ALERT_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_START_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.util.Pair;

import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class AlertDaoTest {

  @Test
  void getAlertStateForNoResultReturnsOff() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursor = createCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    Pair<AlertState, Long> alertState = dao.getAlertState();

    assertThat(alertState.first).isEqualTo(AlertState.OFF);
    assertThat(alertState.second).isNull();
  }

  @Test
  void getAlertStateForNotConfirmedResultReturnsOn() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    Pair<AlertState, Long> alertState = dao.getAlertState();

    assertThat(alertState.first).isEqualTo(AlertState.ON);
    assertThat(alertState.second).isEqualTo(12L);
  }

  @Test
  void getAlertStateForConfirmedResultReturnsConfirmed() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, true)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    Pair<AlertState, Long> alertState = dao.getAlertState();

    assertThat(alertState.first).isEqualTo(AlertState.ON_CONFIRMED);
    assertThat(alertState.second).isEqualTo(12L);
  }

  @Test
  void getAlertMoreThenOneResultReturnsFirst() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursor = createCursor(Arrays.asList(Pair.create(12L, false), Pair.create(13L, true)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    Pair<AlertState, Long> alertState = dao.getAlertState();

    assertThat(alertState.first).isEqualTo(AlertState.ON);
    assertThat(alertState.second).isEqualTo(12L);
  }

  @Test
  void loadAllForNoAlertsReturnsEmptyList() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursor = createAlertCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, ALERT_COLUMNS, null, new String[0], null, null, TABLE_ALERT_COLUMN_START_TIME + " DESC", null))
        .thenReturn(cursor);

    List<Alert> alerts = dao.loadAll();

    assertThat(alerts).isEmpty();
  }

  @Test
  void loadAllForAlertsReturnsAlertsList() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Alert alert1 = new Alert(12L, now.minusSeconds(3), now.minusSeconds(2), true, now.minusSeconds(1), Collections.emptyList());
    Alert alert2 = new Alert(13L, now.plusSeconds(1), now.plusSeconds(2), true, null, Collections.emptyList());
    Alert alert3 = new Alert(14L, now.plusSeconds(1), null, false, null, Collections.emptyList());
    Cursor cursor = createAlertCursor(Arrays.asList(
        alert1,
        alert2,
        alert3
    ));
    when(db.query(TABLE_ALERT, ALERT_COLUMNS, null, new String[0], null, null, TABLE_ALERT_COLUMN_START_TIME + " DESC", null))
        .thenReturn(cursor);

    List<Alert> alerts = dao.loadAll();

    assertThat(alerts.size()).isEqualTo(3);
    assertThat(alerts.get(0).toString()).isEqualTo(alert1.toString());
    assertThat(alerts.get(1).toString()).isEqualTo(alert2.toString());
    assertThat(alerts.get(2).toString()).isEqualTo(alert3.toString());
  }

  @Test
  void loadWithNoCallList() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    Alert expectedAlert = new Alert(12L, now.minusSeconds(3), now.minusSeconds(2), true, now.minusSeconds(1), Collections.emptyList());
    Cursor cursorAlert = createAlertCursor(Collections.singletonList(expectedAlert));
    Cursor cursorAlertCalls = createAlertCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, ALERT_COLUMNS, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(12L)}, null, null, null))
        .thenReturn(cursorAlert);
    when(db.query(TABLE_ALERT_CALL, CALL_COLUMNS, TABLE_ALERT_CALL_COLUMN_ALERT_ID + "=?", new String[]{String.valueOf(12L)}, null, null, TABLE_ALERT_CALL_COLUMN_TIME))
        .thenReturn(cursorAlertCalls);

    Alert alert = dao.load(12L);

    assertThat(alert.toString()).isEqualTo(expectedAlert.toString());
  }

  @Test
  void loadWithCallList() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    List<AlertCall> calls = Arrays.asList(
        new AlertCall(now.plusMillis(100), "msg1"),
        new AlertCall(now.plusMillis(200), "msg2")
    );
    Alert expectedAlert = new Alert(12L, now.minusSeconds(3), now.minusSeconds(2), true, now.minusSeconds(1), calls);
    Cursor cursorAlert = createAlertCursor(Collections.singletonList(expectedAlert));
    Cursor cursorAlertCalls = createAlertCallCursor(calls);
    when(db.query(TABLE_ALERT, ALERT_COLUMNS, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(12L)}, null, null, null))
        .thenReturn(cursorAlert);
    when(db.query(TABLE_ALERT_CALL, CALL_COLUMNS, TABLE_ALERT_CALL_COLUMN_ALERT_ID + "=?", new String[]{String.valueOf(12L)}, null, null, TABLE_ALERT_CALL_COLUMN_TIME))
        .thenReturn(cursorAlertCalls);

    Alert alert = dao.load(12L);

    assertThat(alert.toString()).isEqualTo(expectedAlert.toString());
  }

  @Test
  void loadWithUnknownIdThrowsException() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);

    Cursor cursorAlert = createAlertCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, ALERT_COLUMNS, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(12L)}, null, null, null))
        .thenReturn(cursorAlert);
    Exception exception = assertThrows(IllegalStateException.class, () -> dao.load(12L));
    assertThat(exception.getMessage()).isEqualTo("No alert found with id: 12");
  }

  @Test
  void insertOrUpdateAlertForAlarmStateOffNotConfirmedReturnsTrue() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant startTime = Instant.now();
    boolean reTriggered = dao.insertOrUpdateAlert(startTime, "text", false);
    assertThat(reTriggered).isTrue();
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verify(db).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void insertOrUpdateAlertForAlarmStateOffConfirmedReturnsTrue() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.emptyList());
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant startTime = Instant.now();
    boolean reTriggered = dao.insertOrUpdateAlert(startTime, "text", true);
    assertThat(reTriggered).isTrue();
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verify(db).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void insertOrUpdateAlertForAlarmStateOnConfirmedReturnsTrue() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, true)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant startTime = Instant.now();
    boolean reTriggered = dao.insertOrUpdateAlert(startTime, "text", false);
    assertThat(reTriggered).isTrue();
    verify(db).update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_ID + "=?"), eq(new String[]{String.valueOf(12L)}));
    verify(db).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void insertOrUpdateAlertForAlarmStateOnReturnsFalse() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant startTime = Instant.now();
    boolean reTriggered = dao.insertOrUpdateAlert(startTime, "text", false);
    assertThat(reTriggered).isFalse();
    verify(db).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void saveImportedAlertNotConfirmed() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant now = Instant.now();
    List<AlertCall> calls = Arrays.asList(new AlertCall(now, "msg1"), new AlertCall(now, "msg2"));
    dao.saveImportedAlert(new Alert(12L, now, now, false, null, calls));
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verify(db, times(calls.size())).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void saveImportedAlertConfirmed() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant now = Instant.now();
    List<AlertCall> calls = Arrays.asList(new AlertCall(now, "msg1"), new AlertCall(now, "msg2"));
    dao.saveImportedAlert(new Alert(12L, now, now, true, null, calls));
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verify(db, times(calls.size())).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void saveImportedAlertConfirmedAndClosed() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant now = Instant.now();
    List<AlertCall> calls = Arrays.asList(new AlertCall(now, "msg1"), new AlertCall(now, "msg2"));
    dao.saveImportedAlert(new Alert(12L, now, now, true, now, calls));
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verify(db, times(calls.size())).insert(eq(TABLE_ALERT_CALL), isNull(), notNull());
  }

  @Test
  void saveImportedAlertNoCalls() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);
    Cursor cursor = createCursor(Collections.singletonList(Pair.create(12L, false)));
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null))
        .thenReturn(cursor);

    AlertDao dao = new AlertDao(dbFactory);
    Instant now = Instant.now();
    dao.saveImportedAlert(new Alert(12L, now, now, false, null, Collections.emptyList()));
    verify(db).insert(eq(TABLE_ALERT), isNull(), notNull());
    verifyNoMoreInteractions(db);
  }

  @Test
  void confirmOpenAlertWithConfirmTime() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(0)).thenReturn(1000L);
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
        null, null, null, null)).thenReturn(cursor);

    when(db.update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);

    AlertDao dao = new AlertDao(dbFactory);
    dao.confirmOpenAlert();

    verify(db).update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void confirmOpenAlertWithNoConfirmTime() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
        null, null, null, null)).thenReturn(cursor);

    when(db.update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);

    AlertDao dao = new AlertDao(dbFactory);
    dao.confirmOpenAlert();

    verify(db).update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void confirmOpenAlertWithEmptyConfirmTime() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(0)).thenReturn(0L);
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
        null, null, null, null)).thenReturn(cursor);

    when(db.update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(0);

    AlertDao dao = new AlertDao(dbFactory);
    dao.confirmOpenAlert();

    verify(db).update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void confirmOpenAlertWithWithUnExpectedRecordUpdateOfNonOne() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.moveToFirst()).thenReturn(false);
    when(db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
        null, null, null, null)).thenReturn(cursor);

    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);

    when(db.update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);

    AlertDao dao = new AlertDao(dbFactory);
    dao.confirmOpenAlert();

    verify(db).update(eq(TABLE_ALERT), notNull(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void closeOpenAlertWithExpectedRecordUpdateOfOne() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(1);

    AlertDao dao = new AlertDao(dbFactory);
    dao.closeOpenAlert();

    verify(db).update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void closeOpenAlertWithUnExpectedRecordUpdateOfNonOne() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    when(db.update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull())).thenReturn(2);

    AlertDao dao = new AlertDao(dbFactory);
    dao.closeOpenAlert();

    verify(db).update(eq(TABLE_ALERT), any(), eq(TABLE_ALERT_COLUMN_END_TIME + " IS NULL"), isNull());
  }

  @Test
  void delete() {
    Alert alert = new Alert(12L, Instant.now(), Instant.now(), false, null, Arrays.asList(
        new AlertCall(Instant.now(), "msg1"),
        new AlertCall(Instant.now(), "msg2")
    ));

    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    AlertDao dao = new AlertDao(dbFactory);
    dao.delete(alert);

    verify(db).delete(TABLE_ALERT, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alert.getId())});
  }

  private Cursor createCursor(List<Pair<Long, Boolean>> alertStates) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(alertStates.size());
    if (!alertStates.isEmpty()) {
      Boolean[] moveResults = new Boolean[alertStates.size()];
      Arrays.fill(moveResults, true);
      moveResults[moveResults.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(alertStates.size() > 1, moveResults);

      List<Long> ids = alertStates.stream().map(pair -> pair.first).collect(Collectors.toList());
      List<Integer> confirmedStates = alertStates.stream().map(pair -> pair.second ? 1 : 0).collect(Collectors.toList());

      when(cursor.getLong(0)).thenReturn(ids.get(0), ids.subList(1, ids.size()).toArray(new Long[0]));
      when(cursor.getInt(1)).thenReturn(confirmedStates.get(0), confirmedStates.subList(1, confirmedStates.size()).toArray(new Integer[0]));
    }
    return cursor;
  }

  private Cursor createAlertCursor(List<Alert> alerts) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(alerts.size());
    when(cursor.moveToFirst()).thenReturn(!alerts.isEmpty());
    if (!alerts.isEmpty()) {
      Boolean[] moveResults = new Boolean[alerts.size() - 1];
      Arrays.fill(moveResults, true);
      if (moveResults.length > 1) {
        moveResults[moveResults.length - 1] = false;
      }
      when(cursor.moveToNext()).thenReturn(alerts.size() > 1, moveResults);

      List<Long> ids = alerts.stream().map(Alert::getId).collect(Collectors.toList());
      List<Long> startTimes = alerts.stream().map(Alert::getStartTime).map(Instant::toEpochMilli).collect(Collectors.toList());
      List<Long> confirmTimes = alerts.stream().map(Alert::getConfirmTime).map(instant -> instant != null ? instant.toEpochMilli() : 0).collect(Collectors.toList());
      List<Integer> confirmedStates = alerts.stream().map(alert -> alert.isConfirmed() ? 1 : 0).collect(Collectors.toList());
      List<Long> endTimes = alerts.stream().map(Alert::getEndTime).map(instant -> instant != null ? instant.toEpochMilli() : 0).collect(Collectors.toList());

      when(cursor.getLong(0)).thenReturn(ids.get(0), ids.subList(1, ids.size()).toArray(new Long[0]));
      when(cursor.getLong(1)).thenReturn(startTimes.get(0), startTimes.subList(1, confirmedStates.size()).toArray(new Long[0]));
      when(cursor.getLong(2)).thenReturn(confirmTimes.get(0), confirmTimes.subList(1, confirmedStates.size()).toArray(new Long[0]));
      when(cursor.getInt(3)).thenReturn(confirmedStates.get(0), confirmedStates.subList(1, confirmedStates.size()).toArray(new Integer[0]));
      when(cursor.getLong(4)).thenReturn(endTimes.get(0), endTimes.subList(1, confirmedStates.size()).toArray(new Long[0]));
    }
    return cursor;
  }

  private Cursor createAlertCallCursor(List<AlertCall> alertCalls) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(alertCalls.size());
    when(cursor.moveToFirst()).thenReturn(!alertCalls.isEmpty());
    if (!alertCalls.isEmpty()) {
      Boolean[] moveResults = new Boolean[alertCalls.size() - 1];
      Arrays.fill(moveResults, true);
      moveResults[moveResults.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(alertCalls.size() > 1, moveResults);

      List<Long> times = alertCalls.stream().map(AlertCall::getTime).map(Instant::toEpochMilli).collect(Collectors.toList());
      List<String> messages = alertCalls.stream().map(AlertCall::getMessage).collect(Collectors.toList());

      when(cursor.getLong(0)).thenReturn(times.get(0), times.subList(1, times.size()).toArray(new Long[0]));
      when(cursor.getString(1)).thenReturn(messages.get(0), messages.subList(1, messages.size()).toArray(new String[0]));
    }
    return cursor;
  }
}