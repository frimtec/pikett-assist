package com.github.frimtec.android.pikettassist.service.dao;

import static com.github.frimtec.android.pikettassist.service.dao.AlertDao.AlertStateChange.AUTO_CONFIRMED;
import static com.github.frimtec.android.pikettassist.service.dao.AlertDao.AlertStateChange.TRIGGER;
import static com.github.frimtec.android.pikettassist.service.dao.AlertDao.AlertStateChange.UNCHANGED;
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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.core.util.Pair;

import com.github.frimtec.android.pikettassist.domain.Alert;
import com.github.frimtec.android.pikettassist.domain.Alert.AlertCall;
import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlertDao {

  private static final String TAG = "AlertDao";

  static final String[] ALERT_COLUMNS = {
      TABLE_ALERT_COLUMN_ID,
      TABLE_ALERT_COLUMN_START_TIME,
      TABLE_ALERT_COLUMN_CONFIRM_TIME,
      TABLE_ALERT_COLUMN_IS_CONFIRMED,
      TABLE_ALERT_COLUMN_END_TIME
  };
  static final String[] CALL_COLUMNS = {
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

  public enum AlertStateChange {
    TRIGGER,
    AUTO_CONFIRMED,
    UNCHANGED
  }

  public AlertStateChange insertOrUpdateAlert(Instant startTime, String text, boolean confirmed, Duration autoConfirmTime) {
    AlertStateChange alertStateChange;
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
      alertStateChange = TRIGGER;
    } else if (alarmState.first == AlertState.ON_CONFIRMED) {
      alertId = alarmState.second;
      if (isAutoConfirmAllowed(autoConfirmTime, alertId)) {
        Log.i(TAG, "Auto confirm alert");
        alertStateChange = AUTO_CONFIRMED;
      } else {
        Log.i(TAG, "Alarm state ON_CONFIRMED -> ON");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, confirmedDbValue);
        db.update(TABLE_ALERT, contentValues, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alertId)});
        alertStateChange = TRIGGER;
      }
    } else {
      Log.i(TAG, "Alarm state ON -> ON");
      alertId = alarmState.second;
      alertStateChange = UNCHANGED;
    }
    ContentValues contentValues = new ContentValues();
    contentValues.put(TABLE_ALERT_CALL_COLUMN_ALERT_ID, alertId);
    contentValues.put(TABLE_ALERT_CALL_COLUMN_TIME, startTime.toEpochMilli());
    contentValues.put(TABLE_ALERT_CALL_COLUMN_MESSAGE, text);
    db.insert(TABLE_ALERT_CALL, null, contentValues);
    return alertStateChange;
  }

  private boolean isAutoConfirmAllowed(Duration autoConfirmTime, Long alertId) {
    long autoConfirmTimeMillis = autoConfirmTime.toMillis();
    return autoConfirmTimeMillis > 0 && Instant.now().toEpochMilli() - load(alertId).calls().stream().map(AlertCall::time).mapToLong(Instant::toEpochMilli).max().orElse(0) <= autoConfirmTimeMillis;
  }

  public void saveImportedAlert(Alert alert) {
    SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE);
    ContentValues contentValues = new ContentValues();
    contentValues.put(TABLE_ALERT_COLUMN_START_TIME, alert.startTime().toEpochMilli());
    contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, alert.confirmed() ? BOOLEAN_TRUE : BOOLEAN_FALSE);
    if (alert.confirmed()) {
      contentValues.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, alert.confirmTime().toEpochMilli());
    }
    if (alert.endTime() != null) {
      contentValues.put(TABLE_ALERT_COLUMN_END_TIME, alert.endTime().toEpochMilli());
    }
    Long alertId = db.insert(TABLE_ALERT, null, contentValues);
    for (AlertCall call : alert.calls()) {
      contentValues = new ContentValues();
      contentValues.put(TABLE_ALERT_CALL_COLUMN_ALERT_ID, alertId);
      contentValues.put(TABLE_ALERT_CALL_COLUMN_TIME, call.time().toEpochMilli());
      contentValues.put(TABLE_ALERT_CALL_COLUMN_MESSAGE, call.message());
      db.insert(TABLE_ALERT_CALL, null, contentValues);
    }
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
    db.delete(TABLE_ALERT, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alert.id())});
  }

}
