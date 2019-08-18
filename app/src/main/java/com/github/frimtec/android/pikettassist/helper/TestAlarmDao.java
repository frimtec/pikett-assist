package com.github.frimtec.android.pikettassist.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.state.PikettAssist;

public class TestAlarmDao {
  public static void updateAlarmState(String id, DualState state) {
    try (SQLiteDatabase db = PikettAssist.getWritableDatabase()) {
      ContentValues contentValues = new ContentValues();
      contentValues.put("alert_state", state.name());
      db.update("t_test_alarm_state", contentValues, "_id=?", new String[]{id});
    }
  }
}
