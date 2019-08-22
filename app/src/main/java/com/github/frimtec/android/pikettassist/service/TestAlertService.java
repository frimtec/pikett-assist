package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.TestAlarmDao;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;

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
    Context context = getApplicationContext();
    this.pikettState = SharedState.getPikettState(context);
    Log.d(TAG, "Service cycle; pikett state: " + pikettState + "; initial: " + initial);
    if (!initial && this.pikettState == DualState.ON) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(SharedState.getTestAlarmAcceptTimeWindowMinutes(context));
      Set<String> missingTestAlarmContexts = SharedState.getSuperviseTestContexts(context).stream()
          .filter(tc -> !isTestMessageAvailable(tc, messageAcceptedTime.toInstant()))
          .collect(Collectors.toSet());
      missingTestAlarmContexts.forEach(testContext -> {
        Log.w(TAG, "Not received test messages: " + testContext);
        TestAlarmDao.updateAlarmState(testContext, DualState.ON);
      });

      if (!missingTestAlarmContexts.isEmpty()) {
        NotificationHelper.notifyMissingTestAlarm(
            context,
            new Intent(context, MainActivity.class),
            missingTestAlarmContexts
        );
        context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      }
    }
  }

  private boolean isTestMessageAvailable(String testAlarmContext, Instant messageAcceptedTime) {
    try (SQLiteDatabase db = PAssist.getReadableDatabase()) {
      try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{testAlarmContext}, null, null, null)) {
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
          return Instant.ofEpochMilli(cursor.getLong(0))
              .isAfter(messageAcceptedTime);
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
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime nextRun = getTodaysCheckTime(now);
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

  private ZonedDateTime getTodaysCheckTime(ZonedDateTime now) {
    String[] testAlarmCheckTime = SharedState.getTestAlarmCheckTime(getApplicationContext()).split(":");
    return now.truncatedTo(ChronoUnit.MINUTES)
        .with(ChronoField.HOUR_OF_DAY, Integer.parseInt(testAlarmCheckTime[0]))
        .with(ChronoField.MINUTE_OF_HOUR, Integer.parseInt(testAlarmCheckTime[1]));
  }
}
