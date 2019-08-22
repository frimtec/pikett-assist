package com.github.frimtec.android.pikettassist.state;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class PAssist extends Application {

  private static DbHelper openHelper;

  public static SQLiteDatabase getWritableDatabase() {
    return openHelper.getWritableDatabase();
  }

  public static SQLiteDatabase getReadableDatabase() {
    return openHelper.getReadableDatabase();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    openHelper = new DbHelper(this);
    getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
  }
}
