package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.PAssist;

import java.time.Instant;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;

public class AlarmActionListener extends BroadcastReceiver {

  private static final String TAG = "ConfirmListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (ACTION_CLOSE_ALARM.equals(action)) {
      closeAlarm(context);
    } else {
      Log.e(TAG, "Unknown action: " + action);
    }
  }

  private void closeAlarm(Context context) {
    try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put("end_time", Instant.now().toEpochMilli());
      int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " is null", null);
      if (update != 1) {
        Log.e(TAG, "One open case expected, but got " + update);
      }
    }
    NotificationHelper.cancel(context, NotificationHelper.ALERT_NOTIFICATION_ID);
    context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
  }
}
