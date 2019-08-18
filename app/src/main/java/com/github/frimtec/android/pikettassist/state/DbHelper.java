package com.github.frimtec.android.pikettassist.state;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
  private static final String TAG = "DbHelper";

  private static final int DB_VERSION = 2;

  public DbHelper(@Nullable Context context) {
    super(context, "pikett-assist.db", null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    Log.v(TAG, "Create DB");
    db.execSQL("CREATE TABLE t_alert (" +
        "  _id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  start_time INTEGER NOT NULL," +
        "  confirm_time INTEGER," +
        "  end_time INTEGER" +
        ");");
    db.execSQL("CREATE TABLE t_alert_call (" +
        "  _id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "  case_id INTEGER REFERENCES t_alert (id) ON DELETE CASCADE," +
        "  time INTEGER NOT NULL," +
        "  message TEXT NOT NULL" +
        ");");
    db.execSQL("CREATE TABLE t_test_alarm_state (" +
        "  _id TEXT PRIMARY KEY," +
        "  last_received_time INTEGER NOT NULL," +
        "  message TEXT NOT NULL" +
        ");");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.v(TAG, String.format("Upgrade DB from %d to %d", oldVersion, newVersion));
    if (newVersion > oldVersion) {
      db.execSQL("ALTER TABLE t_test_alarm_state ADD COLUMN alert_state TEXT DEFAULT 'OFF' NOT NULL");
    }
  }
}
