package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.TestAlarm;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.MainActivity;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class TestAlarmWorkUnit implements WorkUnit {

  private static final String TAG = "TestAlarmService";

  static final String PARAM_INITIAL = "initial";

  private final ApplicationPreferences applicationPreferences;
  private final TestAlarmDao testAlarmDao;
  private final ShiftService shiftService;
  private final NotificationService notificationService;
  private final Runnable alarmTrigger;
  private final Context context;

  public TestAlarmWorkUnit(
      ApplicationPreferences applicationPreferences,
      TestAlarmDao testAlarmDao,
      ShiftService shiftService,
      NotificationService notificationService,
      Runnable alarmTrigger,
      Context context) {
    this.applicationPreferences = applicationPreferences;
    this.testAlarmDao = testAlarmDao;
    this.shiftService = shiftService;
    this.notificationService = notificationService;
    this.alarmTrigger = alarmTrigger;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Data inputData) {
    boolean initial = inputData.getBoolean(PARAM_INITIAL, true);
    OnOffState shiftState = this.shiftService.getShiftState().getState();
    Log.i(TAG, "Service cycle; shift state: " + shiftState + "; initial: " + initial);
    boolean testAlarmEnabled = this.applicationPreferences.getTestAlarmEnabled(context);
    Set<Integer> testAlarmCheckWeekdays = this.applicationPreferences.getTestAlarmCheckWeekdays(context).stream()
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
    if (!testAlarmEnabled || shiftState != OnOffState.ON || testAlarmCheckWeekdays.isEmpty()) {
      return Optional.empty();
    }

    if (!initial) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime messageAcceptedTime = getTodaysCheckTime(now).minusMinutes(this.applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context));
      Set<TestAlarmContext> supervisedTestAlarms = this.applicationPreferences.getSupervisedTestAlarms(context);
      Set<TestAlarmContext> missingTestAlarmContextContexts = supervisedTestAlarms.stream()
          .filter(tc -> !this.testAlarmDao.isTestAlarmReceived(tc, messageAcceptedTime.toInstant()))
          .collect(Collectors.toSet());
      missingTestAlarmContextContexts.forEach(testContext -> {
        Log.i(TAG, "Not received test messages: " + testContext);
        this.testAlarmDao.updateAlertState(testContext, OnOffState.ON);
      });

      if (!missingTestAlarmContextContexts.isEmpty()) {
        Map<TestAlarmContext, TestAlarm> testAlarmsByContext = this.testAlarmDao.loadAll().stream().collect(Collectors.toMap(
            TestAlarm::context,
            testAlarm -> testAlarm
        ));
        this.notificationService.notifyMissingTestAlarm(
            new Intent(context, MainActivity.class),
            missingTestAlarmContextContexts.stream()
                .map(testAlarmContext -> {
                  TestAlarm testAlarm = testAlarmsByContext.get(testAlarmContext);
                  return testAlarm != null ? testAlarm.name() : testAlarmContext.context();
                }).sorted()
                .collect(Collectors.toList())
        );
        this.alarmTrigger.run();
      }
    }

    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime nextRun = getTodaysCheckTime(now);
    if (nextRun.isBefore(now)) {
      nextRun = nextRun.plusDays(1);
    }
    while (!testAlarmCheckWeekdays.contains(nextRun.getDayOfWeek().getValue())) {
      nextRun = nextRun.plusDays(1);
    }
    return Optional.of(new ScheduleInfo(Duration.between(now, nextRun), newIntent -> newIntent.putExtra(PARAM_INITIAL, false)));
  }

  private ZonedDateTime getTodaysCheckTime(ZonedDateTime now) {
    String[] testAlarmCheckTime = this.applicationPreferences.getTestAlarmCheckTime(context).split(":");
    return now.truncatedTo(ChronoUnit.MINUTES)
        .withHour(Integer.parseInt(testAlarmCheckTime[0]))
        .withMinute(Integer.parseInt(testAlarmCheckTime[1]));
  }
}
