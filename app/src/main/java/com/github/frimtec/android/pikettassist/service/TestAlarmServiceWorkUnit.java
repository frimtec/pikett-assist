package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.MainActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;

import org.threeten.bp.Duration;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.ChronoField;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAlarmServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "TestAlarmService";

  private static final String PARAM_INITIAL = "initial";

  private final TestAlarmDao testAlarmDao;
  private final AlarmService alarmService;
  private final ShiftService shiftService;
  private final Context context;

  public TestAlarmServiceWorkUnit(
      TestAlarmDao testAlarmDao,
      AlarmService alarmService,
      ShiftService shiftService,
      Context context) {
    this.testAlarmDao = testAlarmDao;
    this.alarmService = alarmService;
    this.shiftService = shiftService;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    boolean initial = intent.getBooleanExtra(PARAM_INITIAL, true);
    OnOffState shiftState = this.shiftService.getState();
    Log.i(TAG, "Service cycle; shift state: " + shiftState + "; initial: " + initial);
    if (!initial && ApplicationPreferences.getTestAlarmEnabled(context) && shiftState == OnOffState.ON) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(ApplicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context));
      Set<TestAlarmContext> missingTestAlarmContextContexts = ApplicationPreferences.getSupervisedTestAlarms(context).stream()
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

    if (shiftState == OnOffState.OFF) {
      return Optional.empty();
    }
    Set<Integer> testAlarmCheckWeekdays = ApplicationPreferences.getTestAlarmCheckWeekdays(context).stream()
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
    if (testAlarmCheckWeekdays.isEmpty()) {
      return Optional.empty();
    }

    Intent intentExtras = new Intent(context, TestAlarmServiceWorkUnit.class);
    intentExtras.putExtra(PARAM_INITIAL, false);
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime nextRun = getTodaysCheckTime(now);
    if (nextRun.isBefore(now)) {
      nextRun = nextRun.plusDays(1);
    }
    while (!testAlarmCheckWeekdays.contains(nextRun.getDayOfWeek().getValue())) {
      nextRun = nextRun.plusDays(1);
    }
    return Optional.of(new ScheduleInfo(Duration.between(now, nextRun), intentExtras));
  }

  private ZonedDateTime getTodaysCheckTime(ZonedDateTime now) {
    String[] testAlarmCheckTime = ApplicationPreferences.getTestAlarmCheckTime(context).split(":");
    return now.truncatedTo(ChronoUnit.MINUTES)
        .with(ChronoField.HOUR_OF_DAY, Integer.parseInt(testAlarmCheckTime[0]))
        .with(ChronoField.MINUTE_OF_HOUR, Integer.parseInt(testAlarmCheckTime[1]));
  }
}
