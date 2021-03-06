package com.github.frimtec.android.pikettassist.service.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestAlarmDaoTest {

  @Test
  void loadAllContexts() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    List<String> contexts = Arrays.asList("context1", "context2", "context3");
    Cursor cursor = createTestAlertContextCursor(contexts);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, null, null, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    Set<TestAlarmContext> testAlarmContexts = dao.loadAllContexts();
    assertThat(testAlarmContexts).isEqualTo(contexts.stream().map(TestAlarmContext::new).collect(Collectors.toSet()));
  }

  @Test
  void loadAllContextsForEmptyResult() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = createTestAlertContextCursor(Collections.emptyList());
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, null, null, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    Set<TestAlarmContext> testAlarmContexts = dao.loadAllContexts();
    assertThat(testAlarmContexts).isEmpty();
  }

  @Test
  void loadDetailsForNotExistingTestAlarmReturnsEmpty() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(0);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID, TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE, TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    Optional<TestAlarm> testAlarm = dao.loadDetails(testAlarmContext);
    assertThat(testAlarm).isEmpty();
  }

  @Test
  void loadDetailsForExistingTestAlarmReturnsTestAlarm() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToFirst()).thenReturn(true);
    Instant now = Instant.now();
    when(cursor.getLong(1)).thenReturn(now.toEpochMilli());
    when(cursor.getString(2)).thenReturn(OnOffState.ON.name());
    when(cursor.getString(3)).thenReturn("message");
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID, TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE, TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    Optional<TestAlarm> testAlarm = dao.loadDetails(testAlarmContext);
    assertThat(testAlarm).isNotEmpty();
    assertThat(testAlarm.get().getReceivedTime()).isEqualTo(now);
    assertThat(testAlarm.get().getAlertState()).isEqualTo(OnOffState.ON);
    assertThat(testAlarm.get().getMessage()).isEqualTo("message");
  }

  @Test
  void createNewContextForNewContextReturnsTrue() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(0);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean created = dao.createNewContext(testAlarmContext, "message");
    assertThat(created).isTrue();
    verify(db).insert(eq(TABLE_TEST_ALARM_STATE), isNull(), notNull());
  }

  @Test
  void createNewContextForExistingContextReturnsFalse() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(1);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean created = dao.createNewContext(testAlarmContext, "message");
    assertThat(created).isFalse();
    verify(db, never()).insert(any(), any(), any());
  }

  @Test
  void updateReceivedTestAlertForNewTestAlarmContextReturnsTrue() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(0);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean newlyCreated = dao.updateReceivedTestAlert(testAlarmContext, Instant.now(), "text");
    assertThat(newlyCreated).isTrue();
    verify(db).insert(eq(TABLE_TEST_ALARM_STATE), isNull(), isNotNull());
  }

  @Test
  void updateReceivedTestAlertForExistingTestAlarmContextReturnsFalse() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);

    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(1);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean newlyCreated = dao.updateReceivedTestAlert(testAlarmContext, Instant.now(), "text");
    assertThat(newlyCreated).isFalse();
    verify(db).update(eq(TABLE_TEST_ALARM_STATE), notNull(), eq(TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?"), eq(new String[]{testAlarmContext.getContext()}));
  }

  @Test
  void updateAlertState() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    dao.updateAlertState(testAlarmContext, OnOffState.ON);

    verify(db).update(eq(TABLE_TEST_ALARM_STATE), notNull(), eq(TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?"), eq(new String[]{testAlarmContext.getContext()}));
  }

  @Test
  void isTestAlarmReceivedForAcceptTimeAfterReceivedTimeReturnsTrue() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    Instant messageAcceptedTime = Instant.now();
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(0)).thenReturn(messageAcceptedTime.toEpochMilli());
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean received = dao.isTestAlarmReceived(testAlarmContext, messageAcceptedTime.minusMillis(1));
    assertThat(received).isTrue();
  }

  @Test
  void isTestAlarmReceivedForAcceptTimeEqualsReceivedTimeReturnsFalse() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    Instant messageAcceptedTime = Instant.now();
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getLong(0)).thenReturn(messageAcceptedTime.toEpochMilli());
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean received = dao.isTestAlarmReceived(testAlarmContext, messageAcceptedTime);
    assertThat(received).isFalse();
  }

  @Test
  void isTestAlarmReceivedForMissingDbResultReturnsFalse() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(0);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean received = dao.isTestAlarmReceived(testAlarmContext, Instant.now());
    assertThat(received).isFalse();
  }

  @Test
  void delete() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    dao.delete(testAlarmContext);

    verify(db).delete(TABLE_TEST_ALARM_STATE, TABLE_ALERT_COLUMN_ID + "=?", new String[]{testAlarmContext.getContext()});
  }

  private Cursor createTestAlertContextCursor(List<String> contexts) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(contexts.size());
    when(cursor.moveToFirst()).thenReturn(!contexts.isEmpty());
    if (!contexts.isEmpty()) {
      Boolean[] moveResults = new Boolean[contexts.size() - 1];
      Arrays.fill(moveResults, true);
      moveResults[moveResults.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(contexts.size() > 1, moveResults);
      when(cursor.getString(0)).thenReturn(contexts.get(0), contexts.subList(1, contexts.size()).toArray(new String[0]));
    }
    return cursor;
  }

}