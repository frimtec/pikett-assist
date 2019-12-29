package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;
import com.github.frimtec.android.pikettassist.utility.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Set;
import java.util.stream.Collectors;

public class TestAlarmService extends IntentService {

  private static final String TAG = "TestAlarmService";

  private static final String PARAM_INITIAL = "initial";

  private final TestAlarmDao testAlarmDao;
  private OnOffState pikettState;
  private AlarmManager alarmManager;

  @SuppressWarnings("unused")
  public TestAlarmService() {
    this(TAG, new TestAlarmDao());
  }

  TestAlarmService(String name, TestAlarmDao testAlarmDao) {
    super(name);
    this.testAlarmDao = testAlarmDao;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    boolean initial = intent.getBooleanExtra(PARAM_INITIAL, true);
    Context context = getApplicationContext();
    this.pikettState = CalendarEventHelper.getPikettState(context);
    Log.i(TAG, "Service cycle; pikett state: " + pikettState + "; initial: " + initial);
    if (!initial && SharedState.getTestAlarmEnabled(context) && this.pikettState == OnOffState.ON) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(SharedState.getTestAlarmAcceptTimeWindowMinutes(context));
      Set<TestAlarmContext> missingTestAlarmContextContexts = SharedState.getSupervisedTestAlarms(context).stream()
          .filter(tc -> !this.testAlarmDao.isTestAlarmReceived(tc, messageAcceptedTime.toInstant()))
          .collect(Collectors.toSet());
      missingTestAlarmContextContexts.forEach(testContext -> {
        Log.i(TAG, "Not received test messages: " + testContext);
        new TestAlarmDao().updateAlertState(testContext, OnOffState.ON);
      });

      if (!missingTestAlarmContextContexts.isEmpty()) {
        NotificationHelper.notifyMissingTestAlarm(
            context,
            new Intent(context, MainActivity.class),
            missingTestAlarmContextContexts
        );
        MissingTestAlarmAlarmActivity.trigger(context, this.alarmManager);
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (this.pikettState == OnOffState.OFF) {
      Log.i(TAG, "End service");
      return;
    }
    Set<Integer> testAlarmCheckWeekdays = SharedState.getTestAlarmCheckWeekdays(getApplicationContext()).stream()
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
    if (testAlarmCheckWeekdays.isEmpty()) {
      return;
    }

    Intent intent = new Intent(this, TestAlarmService.class);
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
    Log.i(TAG, "Next run at " + nextRun + "; wait ms: " + waitMs);
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + waitMs,
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
