package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import static com.github.frimtec.android.pikettassist.domain.OnOffState.OFF;

public class DbHelper extends SQLiteOpenHelper {

  public static final String TABLE_ALERT = "t_alert";
  public static final String TABLE_ALERT_COLUMN_ID = "_id";
  public static final String TABLE_ALERT_COLUMN_START_TIME = "start_time";
  public static final String TABLE_ALERT_COLUMN_CONFIRM_TIME = "confirm_time";
  public static final String TABLE_ALERT_COLUMN_IS_CONFIRMED = "is_confirmed";
  public static final String TABLE_ALERT_COLUMN_END_TIME = "end_time";

  public static final String TABLE_ALERT_CALL = "t_alert_call";
  public static final String TABLE_ALERT_CALL_COLUMN_ID = "_id";
  public static final String TABLE_ALERT_CALL_COLUMN_ALERT_ID = "alert_id";
  public static final String TABLE_ALERT_CALL_COLUMN_TIME = "time";
  public static final String TABLE_ALERT_CALL_COLUMN_MESSAGE = "message";

  public static final String TABLE_TEST_ALARM_STATE = "t_test_alarm_state";
  public static final String TABLE_TEST_ALARM_STATE_COLUMN_ID = "_id";
  public static final String TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME = "last_received_time";
  public static final String TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE = "message";
  public static final String TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE = "alert_state";

  public static final String TABLE_KEY_VALUE = "t_key_value";
  public static final String TABLE_KEY_VALUE_COLUMN_ID = "_id";
  public static final String TABLE_KEY_VALUE_COLUMN_VALUE = "value";

  public static final int BOOLEAN_TRUE = 1;
  public static final int BOOLEAN_FALSE = 0;
  private static final String TAG = "DbHelper";
  private static final String DB_NAME = "PAssist.db";
  private static final int DB_VERSION = 2;

  public DbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.i(TAG, "Create DB");
    db.execSQL("CREATE TABLE " + TABLE_ALERT + " (" +
        "  " + TABLE_ALERT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  " + TABLE_ALERT_COLUMN_START_TIME + " INTEGER NOT NULL," +
        "  " + TABLE_ALERT_COLUMN_CONFIRM_TIME + " INTEGER," +
        "  " + TABLE_ALERT_COLUMN_IS_CONFIRMED + " INTEGER NOT NULL," +
        "  " + TABLE_ALERT_COLUMN_END_TIME + " INTEGER" +
        ");");
    db.execSQL("CREATE TABLE " + TABLE_ALERT_CALL + " (" +
        "  " + TABLE_ALERT_CALL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  " + TABLE_ALERT_CALL_COLUMN_ALERT_ID + " INTEGER REFERENCES " + TABLE_ALERT + " (" + TABLE_ALERT_COLUMN_ID + ") ON DELETE CASCADE," +
        "  " + TABLE_ALERT_CALL_COLUMN_TIME + " INTEGER NOT NULL," +
        "  " + TABLE_ALERT_CALL_COLUMN_MESSAGE + " TEXT NOT NULL" +
        ");");
    db.execSQL("CREATE TABLE " + TABLE_TEST_ALARM_STATE + " (" +
        "  " + TABLE_TEST_ALARM_STATE_COLUMN_ID + " TEXT PRIMARY KEY," +
        "  " + TABLE_TEST_ALARM_STATE_COLUMN_LAST_RECEIVED_TIME + " INTEGER NOT NULL," +
        "  " + TABLE_TEST_ALARM_STATE_COLUMN_MESSAGE + " TEXT NOT NULL," +
        "  " + TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE + " TEXT DEFAULT '" + OFF + "' NOT NULL" +
        ");");
    createVersion2Tables(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.i(TAG, String.format("Upgrade DB from %d to %d", oldVersion, newVersion));
    if (oldVersion < 2) {
      createVersion2Tables(db);
    }
  }

  private void createVersion2Tables(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE " + TABLE_KEY_VALUE + " (" +
        "  " + TABLE_KEY_VALUE_COLUMN_ID + " TEXT PRIMARY KEY," +
        "  " + TABLE_KEY_VALUE_COLUMN_VALUE + " TEXT" +
        ");");
  }
}
