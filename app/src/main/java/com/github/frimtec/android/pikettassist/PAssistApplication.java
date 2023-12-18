package com.github.frimtec.android.pikettassist;

import static android.content.Intent.EXTRA_BUG_REPORT;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.LOW_SIGNAL_FILTER_PREFERENCE;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.github.frimtec.android.pikettassist.service.KeyValueStore;
import com.github.frimtec.android.pikettassist.service.dao.KeyValueDao;
import com.github.frimtec.android.pikettassist.state.DbFactory;
import com.github.frimtec.android.pikettassist.state.DbHelper;
import com.github.frimtec.android.pikettassist.ui.support.SendLogActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

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

  private static final String PREF_KEY_LOW_SIGNAL_FILTER_OLD = "low_signal_filter";

  @Override
  public void onCreate() {
    super.onCreate();
    Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
    openHelper = new DbHelper(this);
    getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
    keyValueStore = new KeyValueStore(new KeyValueDao(DbFactory.instance()));

    doMigrations();
  }

  private void doMigrations() {
    migrateLowSignalFilterPreference();
  }

  /**
   * Migrates preference "low_signal_filter" (linear filter value) to "low_signal_filter_nl" (non linear filter value).
   * Migration between release 1.5.1 to release 1.5.2.
   */
  private void migrateLowSignalFilterPreference() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    if (preferences.getInt(PREF_KEY_LOW_SIGNAL_FILTER, -1) == -1) {
      int oldValue = preferences.getInt(PREF_KEY_LOW_SIGNAL_FILTER_OLD, getResources().getInteger(R.integer.default_low_signal_filter));
      int migratedValue = LOW_SIGNAL_FILTER_PREFERENCE.getIndex(oldValue * PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putInt(PREF_KEY_LOW_SIGNAL_FILTER, migratedValue);
      editor.remove(PREF_KEY_LOW_SIGNAL_FILTER_OLD);
      editor.apply();
      Log.i(TAG, String.format("Migrating low signal filter preference from old value %s to new value %s", oldValue, migratedValue));
    }
  }

  private void handleUncaughtException(Thread thread, Throwable e) {
    Log.e(TAG, "Unhandled exception occurred", e);
    Application application = (Application) this.getApplicationContext();
    Intent intent = new Intent(application, SendLogActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    intent.putExtra(EXTRA_BUG_REPORT, createReport(thread, e));
    application.startActivity(intent);
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
    writer.println("App version: " + (info == null ? "NOT AVAILABLE" : BuildConfig.VERSION_CODE));
    writer.println("Thread name: " + thread.getName());
    writer.println();
    writer.println("Exception stack trace:");
    e.printStackTrace(writer);
    return stringWriter.getBuffer().toString();
  }
}
