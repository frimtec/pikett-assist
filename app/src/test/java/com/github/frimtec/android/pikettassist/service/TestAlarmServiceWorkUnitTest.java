package com.github.frimtec.android.pikettassist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.ShiftState;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class TestAlarmServiceWorkUnitTest {

  public static final HashSet<String> WEEK_DAYS = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5"));
  public static final HashSet<String> ALL_DAYS = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void applyForTestAlarmDisabledShiftOnReturnsEmpty(boolean initial) {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(false);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(WEEK_DAYS);
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn("12:00");

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(initial);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void applyForTestAlarmEnabledShiftOffReturnsEmpty(boolean initial) {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(WEEK_DAYS);
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn("12:00");

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.OFF));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(initial);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void applyForTestNoWeekDaysReturnsEmpty(boolean initial) {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn("12:00");

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(initial);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForInitialCheckTimeBeforeNowReturnsInitialFalseTimeTillTomorrowsCheckTime() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(ALL_DAYS);


    ZonedDateTime checkTime = ZonedDateTime.now().minusMinutes(10);
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(true);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofDays(1).minusMinutes(11), Duration.ofDays(1).minusMinutes(10));
    verify(resultIntent).putExtra("initial", false);
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForInitialCheckTimeAfterNowReturnsInitialFalseTimeTillTodaysCheckTime() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(ALL_DAYS);


    ZonedDateTime checkTime = ZonedDateTime.now().plusMinutes(11);
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(true);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofMinutes(10), Duration.ofMinutes(11));
    verify(resultIntent).putExtra("initial", false);
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForInitialCheckTimeAfterNowWithNextTwoWeekdaysDisabledReturnsInitialFalseTimeTillOverTomorrowsCheckTime() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());

    Set<String> weekdays = new HashSet<>(ALL_DAYS);
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(weekdays);


    ZonedDateTime checkTime = ZonedDateTime.now().plusMinutes(11);
    weekdays.remove(String.valueOf(checkTime.getDayOfWeek().getValue()));
    weekdays.remove(String.valueOf(checkTime.plusDays(1).getDayOfWeek().getValue()));
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    when(testAlarmDao.isTestAlarmReceived(eq(testAlarmContext), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(true);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofDays(2).plusMinutes(10), Duration.ofDays(2).plusMinutes(11));
    verify(resultIntent).putExtra("initial", false);
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(testAlarmContext, OnOffState.ON);
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForNonInitialNotAllAlarmsReceivedWithinTimeReturnsInitialFalseTimeTillTomorrowsCheckTimeRacesAlarm() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    TestAlarmContext tc1 = new TestAlarmContext("tc1");
    TestAlarmContext tc2 = new TestAlarmContext("tc2");
    TestAlarmContext tc3 = new TestAlarmContext("tc3");
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(new HashSet<>(Arrays.asList(tc1, tc2, tc3)));
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(ALL_DAYS);

    ZonedDateTime checkTime = ZonedDateTime.now();
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    when(testAlarmDao.isTestAlarmReceived(eq(tc1), any(Instant.class))).thenReturn(false);
    when(testAlarmDao.isTestAlarmReceived(eq(tc2), any(Instant.class))).thenReturn(true);
    when(testAlarmDao.isTestAlarmReceived(eq(tc3), any(Instant.class))).thenReturn(false);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(false);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofDays(1).minusMinutes(1), Duration.ofDays(1));
    verify(resultIntent).putExtra("initial", false);
    verify(notificationService).notifyMissingTestAlarm(any(Intent.class), eq(new HashSet<>(Arrays.asList(tc1, tc3))));
    verify(testAlarmDao).updateAlertState(tc1, OnOffState.ON);
    verify(testAlarmDao, never()).updateAlertState(tc2, OnOffState.ON);
    verify(testAlarmDao).updateAlertState(tc3, OnOffState.ON);
    verify(alarmTrigger).run();
  }

  @Test
  void applyForNonTestContextInitialReturnsInitialFalseTimeTillTomorrowsCheckTime() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(Collections.emptySet());
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(ALL_DAYS);

    ZonedDateTime checkTime = ZonedDateTime.now();
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    when(testAlarmDao.isTestAlarmReceived(any(TestAlarmContext.class), any(Instant.class))).thenReturn(false);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(false);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofDays(1).minusMinutes(1), Duration.ofDays(1));
    verify(resultIntent).putExtra("initial", false);
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(any(TestAlarmContext.class), eq(OnOffState.ON));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForNonInitialAllAlarmsReceivedWithinTimeReturnsInitialFalseTimeTillTomorrowsCheckTime() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getTestAlarmEnabled(context)).thenReturn(true);
    when(applicationPreferences.getTestAlarmAcceptTimeWindowMinutes(context)).thenReturn(15);
    TestAlarmContext tc1 = new TestAlarmContext("tc1");
    TestAlarmContext tc2 = new TestAlarmContext("tc2");
    TestAlarmContext tc3 = new TestAlarmContext("tc3");
    when(applicationPreferences.getSupervisedTestAlarms(context)).thenReturn(new HashSet<>(Arrays.asList(tc1, tc2, tc3)));
    when(applicationPreferences.getTestAlarmCheckWeekdays(context)).thenReturn(ALL_DAYS);

    ZonedDateTime checkTime = ZonedDateTime.now();
    when(applicationPreferences.getTestAlarmCheckTime(context)).thenReturn(checkTime.format(DateTimeFormatter.ofPattern("HH:mm")));

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    TestAlarmDao testAlarmDao = mock(TestAlarmDao.class);
    when(testAlarmDao.isTestAlarmReceived(eq(tc1), any(Instant.class))).thenReturn(true);
    when(testAlarmDao.isTestAlarmReceived(eq(tc2), any(Instant.class))).thenReturn(true);
    when(testAlarmDao.isTestAlarmReceived(eq(tc3), any(Instant.class))).thenReturn(true);

    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new TestAlarmServiceWorkUnit(
        applicationPreferences,
        testAlarmDao,
        shiftService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getBooleanExtra("initial", true)).thenReturn(false);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isBetween(Duration.ofDays(1).minusMinutes(1), Duration.ofDays(1));
    verify(resultIntent).putExtra("initial", false);
    //noinspection unchecked
    verify(notificationService, never()).notifyMissingTestAlarm(any(Intent.class), any(Set.class));
    verify(testAlarmDao, never()).updateAlertState(any(TestAlarmContext.class), eq(OnOffState.ON));
    verify(alarmTrigger, never()).run();
  }

}