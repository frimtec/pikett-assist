package com.github.frimtec.android.pikettassist.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.github.frimtec.android.pikettassist.utility.Feature;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;

public class AutoStart extends BroadcastReceiver {

  private static final String TAG = "AutoStart";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
      Log.i(TAG, "Received: BOOT_COMPLETED");
      NotificationHelper.registerChannel(context);
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Feature.SETTING_BATTERY_OPTIMIZATION_OFF.isAllowed(context)) {
        Log.i(TAG, "Start PikettService in background");
        context.startService(new Intent(context, PikettService.class));
      } else {
        Log.w(TAG, "Start PikettService not allowed with enabled battery optimization");
      }
    }
  }
}
