package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.helper.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAlertService extends IntentService {

  private static final String TAG = "TestAlertService";

  private static final String PARAM_INITIAL = "initial";

  private DualState pikettState;

  public TestAlertService() {
    super(TAG);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    boolean initial = intent.getBooleanExtra(PARAM_INITIAL, true);
    this.pikettState = SharedState.getPikettState(getApplicationContext());
    Log.d(TAG, "Service cycle; pikett state: " + pikettState + "; initial: " + initial);
    if (!initial && this.pikettState == DualState.ON) {
      // make test
      Log.d(TAG, "Do test");
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(15);
      SharedState.getSuperviseTestContexts(getApplicationContext()).stream()
          .filter(tc -> !isTestMessageAvailable(tc, messageAcceptedTime))
          .forEach(testContext -> {
            Log.w(TAG, "Not received test messages: " + testContext);
            TestAlarmDao.updateAlarmState(testContext, DualState.ON);
            getApplicationContext().sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
          });
    }
  }

  private boolean isTestMessageAvailable(String testAlarmContext, LocalDateTime messageAcceptedTime) {
    try (SQLiteDatabase db = PikettAssist.getReadableDatabase()) {
      try (Cursor cursor = db.query("t_test_alarm_state", new String[]{"last_received_time"}, "_id=?", new String[]{testAlarmContext}, null, null, null)) {
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
          Instant lastReceiveTime = Instant.ofEpochMilli(cursor.getLong(0));
          Instant messageAcceptTimeInstant = messageAcceptedTime.toInstant(ZoneOffset.UTC);
          // FIXME: Test me!
          boolean result = lastReceiveTime.isAfter(messageAcceptTimeInstant);
          Log.d(TAG, "Compare: lastReceiveTime=" + lastReceiveTime + "; messageAcceptTimeInstant=" + messageAcceptTimeInstant + "; result=" + result);
          return result;
        }
      }
    }
    return false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (this.pikettState == DualState.OFF) {
      Log.d(TAG, "End service");
      return;
    }
    Set<Integer> testAlarmCheckWeekdays = SharedState.getTestAlarmCheckWeekdays(getApplicationContext()).stream()
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
    if (testAlarmCheckWeekdays.isEmpty()) {
      Log.d(TAG, "No weekdays to test");
      return;
    }

    AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    Intent intent = new Intent(this, TestAlertService.class);
    intent.putExtra(PARAM_INITIAL, false);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime nextRun = getTodaysCheckTime(now);
    if (nextRun.isBefore(now)) {
      nextRun = nextRun.plusDays(1);
    }
    while (!testAlarmCheckWeekdays.contains(nextRun.getDayOfWeek().getValue())) {
      nextRun = nextRun.plusDays(1);
    }
    long waitMs = Duration.between(now, nextRun).toMillis();
    Log.d(TAG, "Next run at " + nextRun + "; wait ms: " + waitMs);
    alarm.setExactAndAllowWhileIdle(alarm.RTC_WAKEUP, System.currentTimeMillis() + waitMs,
        PendingIntent.getService(this, 0, intent, 0)
    );
  }

  private LocalDateTime getTodaysCheckTime(LocalDateTime now) {
    String[] testAlarmCheckTime = SharedState.getTestAlarmCheckTime(getApplicationContext()).split(":");
    return now.toLocalDate().atTime(Integer.parseInt(testAlarmCheckTime[0]), Integer.parseInt(testAlarmCheckTime[1]));
  }
}
