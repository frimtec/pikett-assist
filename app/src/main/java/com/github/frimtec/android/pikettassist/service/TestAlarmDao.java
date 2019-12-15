package com.github.frimtec.android.pikettassist.service;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.state.DbFactory;

import static com.github.frimtec.android.pikettassist.state.DbFactory.Mode.WRITABLE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALARM_STATE_COLUMN_ID;

public class TestAlarmDao {

  private final DbFactory dbFactory;

  public TestAlarmDao() {
    this(DbFactory.instance());
  }

  TestAlarmDao(DbFactory dbFactory) {
    this.dbFactory = dbFactory;
  }

  public void updateAlarmState(String id, OnOffState state) {
    try (SQLiteDatabase db = this.dbFactory.getDatabase(WRITABLE)) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_TEST_ALARM_STATE_COLUMN_ALERT_STATE, state.name());
      db.update(TABLE_TEST_ALARM_STATE, contentValues, TABLE_TEST_ALARM_STATE_COLUMN_ID + "=?", new String[]{id});
    }
  }
}
