package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class PikettService extends Service {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
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
    Instant now = PikettShift.now();
    Optional<PikettShift> first = CalendarEventHelper.getPikettShifts(this, SharedState.getCalendarEventPikettTitlePattern(this))
        .stream().filter(shift -> !shift.isOver(now)).findFirst();
    Instant nextRun = first.map(shift -> shift.isNow(now) ? shift.getEndTime() : shift.getStartTime()).orElse(now.plus(MAX_SLEEP).plusSeconds(10));
    long waitMs = Math.min(Duration.between(now, nextRun).toMillis(), MAX_SLEEP.toMillis());
    Log.d(TAG, "Next run in " + waitMs);
    AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

    if(first.map(PikettShift::isNow).orElse(false)) {
      NotificationHelper.notifyShiftOn(this);
    } else {
      NotificationHelper.cancel(this, NotificationHelper.SHIFT_NOTIFICATION_ID);
    }
    alarm.set(alarm.RTC_WAKEUP, System.currentTimeMillis() + waitMs,
        PendingIntent.getService(this, 0, new Intent(this, PikettService.class), 0)
    );
  }
}
