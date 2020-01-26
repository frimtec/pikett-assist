package com.github.frimtec.android.pikettassist.service.system;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;

public class AlarmService {

  private final Context context;
  private final AlarmManager alarmManager;

  public AlarmService(Context context) {
    this.context = context;
    this.alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
  }

  public void setAlarmRelative(long triggerInMillis, Intent intent) {
    setAlarmAbsolute(System.currentTimeMillis() + triggerInMillis,
        PendingIntent.getService(context, 0, intent, FLAG_UPDATE_CURRENT)
    );
  }

  public void setAlarmAbsolute(long timeInMillis, PendingIntent intent) {
    this.alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        timeInMillis,
        intent
    );
  }
}
