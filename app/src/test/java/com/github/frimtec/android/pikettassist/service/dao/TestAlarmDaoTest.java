package com.github.frimtec.android.pikettassist.service.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.DbFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TestAlarmDaoTest {

  @Test
  void loadAll() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    List<String> contexts = Arrays.asList("context1", "context2", "context3");
    List<Long> receivedTime = Arrays.asList(100L, 0L, 200L);
    List<OnOffState> states = Arrays.asList(OnOffState.ON, OnOffState.OFF, OnOffState.ON);
    List<String> messages = Arrays.asList("message1", null, "message3");
    List<String> aliases = Arrays.asList("alias1", null, "alias3");
    Cursor cursor = createTestAlertContextCursor(
        contexts,
        receivedTime,
        states,
        messages,
        aliases
    );
    when(db.query(
        TABLE_TEST_ALARM_STATE,
        new String[]{
            TABLE_TEST_ALARM_STATE_COLUMN_ID,
            TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME,
            TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE,
            TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE,
            TABLE_TEST_ALARM_STATE_COLUMN_ALIAS
        },
        null,
        null,
        null,
        null,
        null
    )).thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    List<TestAlarm> testAlarms = dao.loadAll();
    assertThat(testAlarms.stream().map(TestAlarm::context).collect(Collectors.toList()))
        .isEqualTo(contexts.stream().map(TestAlarmContext::new).collect(Collectors.toList()));
    assertThat(testAlarms.stream().map(TestAlarm::receivedTime).collect(Collectors.toList()))
        .isEqualTo(receivedTime.stream().map(i -> i == 0L ? null : Instant.ofEpochMilli(i)).collect(Collectors.toList()));
    assertThat(testAlarms.stream().map(TestAlarm::alertState).collect(Collectors.toList()))
        .isEqualTo(states);
    assertThat(testAlarms.stream().map(TestAlarm::message).collect(Collectors.toList()))
        .isEqualTo(messages);
  }

  @Test
  void loadAllContextsForEmptyResult() {
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(READ_ONLY)).thenReturn(db);
    Cursor cursor = createTestAlertContextCursor(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    );
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, null, null, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    List<TestAlarm> testAlarmContexts = dao.loadAll();
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID, TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE, TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    when(cursor.getLong(1)).thenReturn(now.toEpochMilli());
    when(cursor.getString(2)).thenReturn(OnOffState.ON.name());
    when(cursor.getString(3)).thenReturn("message");
    when(
        db.query(
            TABLE_TEST_ALARM_STATE,
            new String[]{
                TABLE_TEST_ALARM_STATE_COLUMN_ID,
                TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME,
                TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE,
                TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE,
                TABLE_TEST_ALARM_STATE_COLUMN_ALIAS
            },
            TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?",
            new String[]{testAlarmContext.context()}, null, null, null)
    ).thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    Optional<TestAlarm> testAlarm = dao.loadDetails(testAlarmContext);
    assertThat(testAlarm).isNotEmpty();
    assertThat(testAlarm.get().receivedTime()).isEqualTo(now);
    assertThat(testAlarm.get().alertState()).isEqualTo(OnOffState.ON);
    assertThat(testAlarm.get().message()).isEqualTo("message");
  }

  @Test
  void createNewContextForNewContextReturnsTrue() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(0);
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
        .thenReturn(cursor);

    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    boolean newlyCreated = dao.updateReceivedTestAlert(testAlarmContext, Instant.now(), "text");
    assertThat(newlyCreated).isFalse();
    verify(db).update(eq(TABLE_TEST_ALARM_STATE), notNull(), eq(TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?"), eq(new String[]{testAlarmContext.context()}));
  }

  @Test
  void updateAlias() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    dao.updateAlias(testAlarmContext, "alias");

    verify(db).update(eq(TABLE_TEST_ALARM_STATE), notNull(), eq(TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?"), eq(new String[]{testAlarmContext.context()}));
  }

  @Test
  void updateAlertState() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("test");
    DbFactory dbFactory = mock(DbFactory.class);
    SQLiteDatabase db = mock(SQLiteDatabase.class);
    when(dbFactory.getDatabase(WRITABLE)).thenReturn(db);
    TestAlarmDao dao = new TestAlarmDao(dbFactory);
    dao.updateAlertState(testAlarmContext, OnOffState.ON);

    verify(db).update(eq(TABLE_TEST_ALARM_STATE), notNull(), eq(TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?"), eq(new String[]{testAlarmContext.context()}));
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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
    when(db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null))
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

    verify(db).delete(TABLE_TEST_ALARM_STATE, TABLE_ALERT_COLUMN_ID + "=?", new String[]{testAlarmContext.context()});
  }

  private Cursor createTestAlertContextCursor(
      List<String> contexts,
      List<Long> receivedTime,
      List<OnOffState> states,
      List<String> messages,
      List<String> aliases
  ) {
    Cursor cursor = mock(Cursor.class);
    when(cursor.getCount()).thenReturn(contexts.size());
    when(cursor.moveToFirst()).thenReturn(!contexts.isEmpty());
    if (!contexts.isEmpty()) {
      Boolean[] moveResults = new Boolean[contexts.size() - 1];
      Arrays.fill(moveResults, true);
      moveResults[moveResults.length - 1] = false;
      when(cursor.moveToNext()).thenReturn(contexts.size() > 1, moveResults);
      when(cursor.getString(0)).thenReturn(contexts.get(0), contexts.subList(1, contexts.size()).toArray(new String[0]));
      when(cursor.getLong(1)).thenReturn(receivedTime.get(0), receivedTime.subList(1, receivedTime.size()).toArray(new Long[0]));
      when(cursor.getString(2)).thenReturn(states.get(0).toString(), states.stream().map(OnOffState::name).toList().subList(1, states.size()).toArray(new String[0]));
      when(cursor.getString(3)).thenReturn(messages.get(0), messages.subList(1, messages.size()).toArray(new String[0]));
      when(cursor.getString(4)).thenReturn(messages.get(0), aliases.subList(1, aliases.size()).toArray(new String[0]));
    }
    return cursor;
  }

}