package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_LOW_BATTERY_NOTIFICATION_CLOSED_BY_USER;
import static com.github.frimtec.android.pikettassist.service.system.NotificationService.ACTION_ON_CALL_NOTIFICATION_CLOSED_BY_USER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.service.system.BatteryService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

public class NotificationActionListener extends BroadcastReceiver {

  private static final String TAG = "NotificationActionListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case ACTION_CLOSE_ALARM -> {
          new AlertService(context).closeAlert();
          context.sendBroadcast(new Intent(Action.REFRESH.getId()));
        }
        case ACTION_ON_CALL_NOTIFICATION_CLOSED_BY_USER -> PikettWorker.enqueueWork(context);
        case ACTION_LOW_BATTERY_NOTIFICATION_CLOSED_BY_USER -> {
          BatteryStatus batteryStatus = new BatteryService(context).batteryStatus();
          if (batteryStatus.level() <= ApplicationPreferences.instance().getBatteryWarnLevel(context)) {
            new NotificationService(context).notifyBatteryLow(batteryStatus);
          }
        }
        default -> Log.e(TAG, "Unknown action: " + action);
      }
    }
  }
}
