package com.github.frimtec.android.pikettassist;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.KeyValueStore;
import com.github.frimtec.android.pikettassist.service.dao.KeyValueDao;
import com.github.frimtec.android.pikettassist.state.DbFactory;
import com.github.frimtec.android.pikettassist.state.DbHelper;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.PrintWriter;
import java.io.StringWriter;

import static android.content.Intent.EXTRA_BUG_REPORT;
import static com.github.frimtec.android.pikettassist.ui.support.SendLogActivity.ACTION_SEND_LOG;

public class PAssistApplication extends Application {

  private static final String TAG = "PAssistApplication";

  private static DbHelper openHelper;
  private static KeyValueStore keyValueStore;

  public static SQLiteDatabase getWritableDatabase() {
    return openHelper.getWritableDatabase();
  }

  public static SQLiteDatabase getReadableDatabase() {
    return openHelper.getReadableDatabase();
  }

  public static KeyValueStore getKeyValueStore() {
    return keyValueStore;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    AndroidThreeTen.init(this);
    Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    openHelper = new DbHelper(this);
    getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
    keyValueStore = new KeyValueStore(new KeyValueDao(DbFactory.instance()));
  }

  private void handleUncaughtException(Thread thread, Throwable e) {
    Log.e(TAG, "Unhandled exception occurred", e);
    Intent intent = new Intent();
    intent.setAction(ACTION_SEND_LOG);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(EXTRA_BUG_REPORT, createReport(thread, e));
    startActivity(intent);
    Process.killProcess(Process.myPid());
  }

  private String createReport(Thread thread, Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    String model = Build.MODEL;
    if (!model.startsWith(Build.MANUFACTURER))
      model = Build.MANUFACTURER + " " + model;

    PackageManager manager = this.getPackageManager();
    PackageInfo info = null;
    try {
      info = manager.getPackageInfo(this.getPackageName(), 0);
    } catch (PackageManager.NameNotFoundException e2) {
      // ignore
    }

    writer.println("Android version: " + Build.VERSION.SDK_INT);
    writer.println("Device: " + model);
    writer.println("App version: " + (info == null ? "NOT AVAILABLE" : info.versionCode));
    writer.println("Thread name: " + thread.getName());
    writer.println();
    writer.println("Exception stack trace:");
    e.printStackTrace(writer);
    return stringWriter.getBuffer().toString();
  }
}
