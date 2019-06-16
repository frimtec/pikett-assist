package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.github.frimtec.android.pikettassist.state.SharedState;

public class PikettService extends Service {

  private static final String TAG = "PikettService";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    Log.d(TAG, "Current alarm state: " + SharedState.getAlarmState(this));
    Log.d(TAG, "Current pikett state: " + SharedState.getPikettState(this));

    stopSelf();
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    alarm.set(alarm.RTC_WAKEUP, System.currentTimeMillis() + (1000 * 60),
        PendingIntent.getService(this, 0, new Intent(this, PikettService.class), 0)
    );
  }
}
