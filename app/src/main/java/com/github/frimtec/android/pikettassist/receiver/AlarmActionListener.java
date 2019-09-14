package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.AlarmService;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;

public class AlarmActionListener extends BroadcastReceiver {

  private static final String TAG = "ConfirmListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (ACTION_CLOSE_ALARM.equals(action)) {
      new AlarmService(context).closeAlarm();
    } else {
      Log.e(TAG, "Unknown action: " + action);
    }
  }
}
