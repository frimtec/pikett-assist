package com.github.frimtec.android.pikettassist.state;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PikettAssist extends Application {
  private static final String TAG = "PikettAssist";

  private static DbHelper openHelper;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.v(TAG, "Application create");
    openHelper = new DbHelper(this);
    getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
  }

  public static SQLiteDatabase getWritableDatabase() {
    return openHelper.getWritableDatabase();
  }

  public static SQLiteDatabase getReadableDatabase() {
    return openHelper.getReadableDatabase();
  }
}
