package com.github.frimtec.android.pikettassist.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.action.Action;

import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_CLOSE_ALARM;

public class NotificationActionListener extends BroadcastReceiver {

  private static final String TAG = "NotificationActionListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      if (ACTION_CLOSE_ALARM.equals(action)) {
        new AlertService(context).closeAlert();
        context.sendBroadcast(new Intent(Action.REFRESH.getId()));
      } else {
        Log.e(TAG, "Unknown action: " + action);
      }
    }
  }
}
