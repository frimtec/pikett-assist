package com.github.frimtec.android.pikettassist.service.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.READ_ONLY;
import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_FALSE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_ALERT_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_MESSAGE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_START_TIME;

public class AlertDao {

  private static final String TAG = "AlertDao";

  private static final String[] ALERT_COLUMNS = {
      TABLE_ALERT_COLUMN_ID,
      TABLE_ALERT_COLUMN_START_TIME,
      TABLE_ALERT_COLUMN_CONFIRM_TIME,
      TABLE_ALERT_COLUMN_IS_CONFIRMED,
      TABLE_ALERT_COLUMN_END_TIME
  };
  private static final String[] CALL_COLUMNS = {
      TABLE_ALERT_CALL_COLUMN_TIME,
      TABLE_ALERT_CALL_COLUMN_MESSAGE
  };

  private final DbFactory dbFactory;

  public AlertDao() {
    this(DbFactory.instance());
  }

  AlertDao(DbFactory dbFactory) {
    this.dbFactory = dbFactory;
  }

  public Pair<AlertState, Long> getAlertState() {
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    return getAlertState(db);
  }

  public List<Alert> loadAll() {
    List<Alert> alertList = new ArrayList<>();
    SQLiteDatabase db = dbFactory.getDatabase(READ_ONLY);
    try (Cursor cursor = db.query(TABLE_ALERT, ALERT_COLUMNS, null, new String[0], null, null, TABLE_ALERT_COLUMN_START_TIME + " DESC", null)) {
      if (cursor != null && cursor.moveToFirst()) {
        do {
          alertList.add(makeAlert(cursor, Collections.emptyList()));
        } while (cursor.moveToNext());
      }
    }
    return alertList;
  }

  public Alert load(long id) {
    SQLiteDatabase db = this.dbFactory.getDatabase(READ_ONLY);
    try (Cursor alertCursor = db.query(TABLE_ALERT, ALERT_COLUMNS, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
         Cursor callCursor = db.query(TABLE_ALERT_CALL, CALL_COLUMNS, TABLE_ALERT_CALL_COLUMN_ALERT_ID + "=?", new String[]{String.valueOf(id)}, null, null, TABLE_ALERT_CALL_COLUMN_TIME)) {
      if (alertCursor != null && alertCursor.moveToFirst()) {
        List<AlertCall> calls = new ArrayList<>();
        if (callCursor != null && callCursor.moveToFirst()) {
          do {
            calls.add(new AlertCall(toInstant(callCursor.getLong(0)), callCursor.getString(1)));
          } while (callCursor.moveToNext());
        }
        return makeAlert(alertCursor, calls);
      } else {
        throw new IllegalStateException("No alert found with id: " + id);
      }
    }
  }

  private Alert makeAlert(Cursor alertCursor, List<AlertCall> calls) {
    return new Alert(
        alertCursor.getLong(0),
        toInstant(alertCursor.getLong(1)),
        toInstant(alertCursor.getLong(2)),
        alertCursor.getInt(3) == BOOLEAN_TRUE,
        toInstant(alertCursor.getLong(4)),
        calls);
  }

  private Instant toInstant(long millis) {
    return millis > 0 ? Instant.ofEpochMilli(millis) : null;
  }

  private Pair<AlertState, Long> getAlertState(SQLiteDatabase db) {
    try (Cursor cursor = db.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_ID, TABLE_ALERT_COLUMN_IS_CONFIRMED}, TABLE_ALERT_COLUMN_END_TIME + " is null", null, null, null, null)) {
      if (cursor.getCount() == 0) {
        return Pair.create(AlertState.OFF, null);
      } else if (cursor.getCount() > 0)
        if (cursor.getCount() > 1) {
          Log.e(TAG, "More then one open case selected");
        }
      cursor.moveToFirst();
      long id = cursor.getLong(0);
      boolean confirmed = cursor.getInt(1) == BOOLEAN_TRUE;
      return Pair.create(confirmed ? AlertState.ON_CONFIRMED : AlertState.ON, id);
    }
  }

  public boolean insertOrUpdateAlert(Instant startTime, String text, boolean confirmed) {
    boolean reTriggered;
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    Pair<AlertState, Long> alarmState = getAlertState(db);
    Long alertId;
    int confirmedDbValue = confirmed ? BOOLEAN_TRUE : BOOLEAN_FALSE;
    if (alarmState.first == AlertState.OFF) {
      Log.i(TAG, "Alarm state OFF -> ON");
      ContentValues contentValues = new ContentValues();
      long startTimeEpochMillis = startTime.toEpochMilli();
      contentValues.put(TABLE_ALERT_COLUMN_START_TIME, startTimeEpochMillis);
      contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, confirmedDbValue);
      if (confirmed) {
        contentValues.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, startTimeEpochMillis);
      }
      alertId = db.insert(TABLE_ALERT, null, contentValues);
      reTriggered = true;
    } else if (alarmState.first == AlertState.ON_CONFIRMED) {
      Log.i(TAG, "Alarm state ON_CONFIRMED -> ON");
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, confirmedDbValue);
      alertId = alarmState.second;
      db.update(TABLE_ALERT, contentValues, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alertId)});
      reTriggered = true;
    } else {
      Log.i(TAG, "Alarm state ON -> ON");
      alertId = alarmState.second;
      reTriggered = false;
    }
    ContentValues contentValues = new ContentValues();
    contentValues.put(TABLE_ALERT_CALL_COLUMN_ALERT_ID, alertId);
    contentValues.put(TABLE_ALERT_CALL_COLUMN_TIME, startTime.toEpochMilli());
    contentValues.put(TABLE_ALERT_CALL_COLUMN_MESSAGE, text);
    db.insert(TABLE_ALERT_CALL, null, contentValues);
    return reTriggered;
  }

  public void confirmOpenAlert() {
    SQLiteDatabase writableDatabase = this.dbFactory.getDatabase(WRITABLE);
    try (Cursor cursor = writableDatabase.query(TABLE_ALERT,
        new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME},
        TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
        null, null, null, null)) {
      ContentValues values = new ContentValues();
      if (!cursor.moveToFirst() || cursor.getLong(0) == 0) {
        values.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, Instant.now().toEpochMilli());
      }
      values.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
      int update = writableDatabase.update(TABLE_ALERT, values,
          TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null);
      if (update != 1) {
        Log.e(TAG, "One open case expected, but got " + update);
      }
    }
  }

  public void closeOpenAlert() {
    SQLiteDatabase writableDatabase = this.dbFactory.getDatabase(WRITABLE);
    ContentValues values = new ContentValues();
    values.put(TABLE_ALERT_COLUMN_END_TIME, Instant.now().toEpochMilli());
    int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null);
    if (update != 1) {
      Log.e(TAG, "One open case expected, but got " + update);
    }
  }

  public void delete(Alert alert) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    db.delete(TABLE_ALERT, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alert.getId())});
  }

}
