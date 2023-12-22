package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.*;

public class TestAlarmDao {

  private final DbFactory dbFactory;

  public TestAlarmDao() {
    this(DbFactory.instance());
  }

  TestAlarmDao(DbFactory dbFactory) {
    this.dbFactory = dbFactory;
  }

  public List<TestAlarm> loadAll() {
    List<TestAlarm> allTestAlarms = new ArrayList<>();
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(
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
    )) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        do {
          long lastReceivedTime = cursor.getLong(1);
          allTestAlarms.add(new TestAlarm(
              new TestAlarmContext(cursor.getString(0)),
              lastReceivedTime > 0 ? Instant.ofEpochMilli(lastReceivedTime) : null,
              OnOffState.valueOf(cursor.getString(2)),
              cursor.getString(3),
              cursor.getString(4)
          ));
        } while (cursor.moveToNext());
      }
    }
    return allTestAlarms;
  }

  public Optional<TestAlarm> loadDetails(TestAlarmContext testAlarmContext) {
    SQLiteDatabase db = dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(
        TABLE_TEST_ALARM_STATE,
        new String[]{
            TABLE_TEST_ALARM_STATE_COLUMN_ID,
            TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME,
            TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE,
            TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE,
            TABLE_TEST_ALARM_STATE_COLUMN_ALIAS
        },
        TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?",
        new String[]{testAlarmContext.context()},
        null,
        null,
        null
    )) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        return Optional.of(new TestAlarm(
            testAlarmContext,
            cursor.getLong(1) > 0 ? Instant.ofEpochMilli(cursor.getLong(1)) : null,
            OnOffState.valueOf(cursor.getString(2)),
            cursor.getString(3),
            cursor.getString(4)
        ));
      }
    }
    return Optional.empty();
  }

  public boolean createNewContext(TestAlarmContext testAlarmContext, String message) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    try (Cursor cursor = db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null)) {
      if (cursor.getCount() == 0) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_ID, testAlarmContext.context());
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, 0);
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE, message);
        db.insert(TABLE_TEST_ALARM_STATE, null, contentValues);
        return true;
      } else {
        return false;
      }
    }
  }

  public boolean updateReceivedTestAlert(TestAlarmContext testAlarm, Instant time, String text) {
    boolean newTestAlarm;
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    String testAlarmContext = testAlarm.context();
    try (Cursor cursor = db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_ID}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext}, null, null, null)) {
      long timeEpochMillis = time.toEpochMilli();
      if (cursor.getCount() == 0) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_ID, testAlarmContext);
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, timeEpochMillis);
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE, text);
        db.insert(TABLE_TEST_ALARM_STATE, null, contentValues);
        newTestAlarm = true;
      } else {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME, timeEpochMillis);
        contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE, text);
        db.update(TABLE_TEST_ALARM_STATE, contentValues, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext});
        newTestAlarm = false;
      }
      return newTestAlarm;
    }
  }

  public void updateAlias(TestAlarmContext testAlarm, String alias) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    String testAlarmContext = testAlarm.context();
    ContentValues contentValues = new ContentValues();
    contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_ALIAS, alias);
    db.update(TABLE_TEST_ALARM_STATE, contentValues, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext});
  }

  public void updateAlertState(TestAlarmContext testAlarmContext, OnOffState state) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    ContentValues contentValues = new ContentValues();
    contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE, state.name());
    db.update(TABLE_TEST_ALARM_STATE, contentValues, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()});
  }

  public boolean isTestAlarmReceived(TestAlarmContext testAlarmContext, Instant messageAcceptedTime) {
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(TABLE_TEST_ALARM_STATE, new String[]{TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext.context()}, null, null, null)) {
      if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
        return Instant.ofEpochMilli(cursor.getLong(0))
            .isAfter(messageAcceptedTime);
      }
    }
    return false;
  }

  public void delete(TestAlarmContext testAlarmContext) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    db.delete(TABLE_TEST_ALARM_STATE, TABLE_ALERT_COLUMN_ID + "=?", new String[]{testAlarmContext.context()});
  }
}
