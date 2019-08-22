package com.github.frimtec.android.pikettassist.state;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PAssist extends Application {
  private static final String TAG = "PAssist";

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
    Log.v(TAG, "Application create");
    openHelper = new DbHelper(this);
    getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
  }
}
