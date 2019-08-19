package com.github.frimtec.android.pikettassist.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

import static com.github.frimtec.android.pikettassist.state.DbHelper.*;

public class TestAlarmDao {
  public static void updateAlarmState(String id, DualState state) {
    try (SQLiteDatabase db = PikettAssist.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_ALERT_STATE, state.name());
      db.update(TABLE_TEST_ALERT_STATE, contentValues, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{id});
    }
  }
}
