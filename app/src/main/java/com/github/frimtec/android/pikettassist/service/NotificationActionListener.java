package com.github.frimtec.android.pikettassist.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.github.frimtec.android.pikettassist.utility.NotificationHelper.ACTION_CLOSE_ALARM;

public class NotificationActionListener extends BroadcastReceiver {

  private static final String TAG = "NotificationActionListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case ACTION_CLOSE_ALARM:
          new AlarmService(context).closeAlarm();
          break;
        default:
          Log.e(TAG, "Unknown action: " + action);
      }
    }
  }
}
