package com.github.frimtec.android.pikettassist.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;

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
  private OnOffState shiftState;
  private AlarmService alarmService;
  private ShiftService shiftService;

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
    this.alarmService = new AlarmService(this);
    this.shiftService = new ShiftService(this);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    boolean initial = intent.getBooleanExtra(PARAM_INITIAL, true);
    Context context = getApplicationContext();
    this.shiftState = this.shiftService.getState();
    Log.i(TAG, "Service cycle; shift state: " + shiftState + "; initial: " + initial);
    if (!initial && SharedState.getTestAlarmEnabled(context) && this.shiftState == OnOffState.ON) {
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
        new NotificationService(context).notifyMissingTestAlarm(
            new Intent(context, MainActivity.class),
            missingTestAlarmContextContexts
        );
        MissingTestAlarmAlarmActivity.trigger(context, this.alarmService);
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (this.shiftState == OnOffState.OFF) {
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
    alarmService.setAlarmRelative(waitMs, intent);
  }

  private ZonedDateTime getTodaysCheckTime(ZonedDateTime now) {
    String[] testAlarmCheckTime = SharedState.getTestAlarmCheckTime(getApplicationContext()).split(":");
    return now.truncatedTo(ChronoUnit.MINUTES)
        .with(ChronoField.HOUR_OF_DAY, Integer.parseInt(testAlarmCheckTime[0]))
        .with(ChronoField.MINUTE_OF_HOUR, Integer.parseInt(testAlarmCheckTime[1]));
  }
}
