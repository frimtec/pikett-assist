package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import androidx.core.util.Pair;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

import java.util.Optional;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_OFFHOOK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LowSignalServiceWorkUnitTest {

  @Test
  void applyForPikettStateOffReturnsEmpty() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(30);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(true);
    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(true);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.OFF);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.OFF;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalOkIsDayReturnsDailyPeriod() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(30);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(true);
    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(true);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalOkIsNightReturnsNightlyPeriod() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(30);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(true);

    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofMinutes(15));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalOkIsNightEndReturnsDailyPeriod() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(30);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(true);

    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(false, true);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalNokNoFilterIsDayReturnsDailyPeriodRaceAlarmAndNotify() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService).notifySignalLow(SignalLevel.POOR);
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger).run();
  }

  @Test
  void applyForPikettStateOnSignalNokFilterIsDayReturnsDailyPeriodWithIncreasedFilter() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(45);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(15);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(15));
    verify(resultIntent).putExtra("FILTER_STATE", 30);
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalOkFilterIsDayReturnsDailyPeriodWithResetFilter() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(45);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(15);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }


  @Test
  void applyForPikettStateOnSignalOffFilterIsDayReturnsDailyPeriodWithIncreasedFilterRaceAlarm() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(45);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.OFF;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(15);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(15));
    verify(resultIntent).putExtra("FILTER_STATE", 15);
    verify(notificationService).notifySignalLow(SignalLevel.OFF);
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger).run();
  }

  @Test
  void applyForPikettStateOnSignalNokFilterMaxIsDayReturnsDailyPeriodWithIncreasedFilterRaceAlarm() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(45);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(45);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent).putExtra("FILTER_STATE", 46);
    verify(notificationService).notifySignalLow(SignalLevel.POOR);
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger).run();
  }

  @Test
  void applyForPikettStateOnSignalNokNoFilterCallStateOffHookIsDayReturnsDailyPeriod() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_OFFHOOK);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalNokNoFilterAlarmStateOnIsDayReturnsDailyPeriod() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.ON, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOnSignalNokNoFilterNoNotificationIsDayReturnsDailyPeriodRaceAlarm() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(false);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger).run();
  }

  @Test
  void applyForPikettStateOnVolumeManageOnSetsVolume() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    when(applicationPreferences.getOnCallVolume(eq(context), any(LocalTime.class))).thenReturn(6);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);
    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(true);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.ON);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq("FILTER_STATE"), any(Integer.class));
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService).setVolume(6);
    verify(alarmTrigger, never()).run();
  }

  @Test
  void applyForPikettStateOffVolumeManageOnVolumeNotSet() {
    // arrange
    Context context = mock(Context.class);

    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getSuperviseSignalStrength(context)).thenReturn(true);
    when(applicationPreferences.getLowSignalFilterSeconds(context)).thenReturn(0);
    when(applicationPreferences.getNotifyLowSignal(context)).thenReturn(true);
    when(applicationPreferences.getSuperviseSignalStrengthMinLevel(context)).thenReturn(2);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    when(applicationPreferences.getOnCallVolume(eq(context), any(LocalTime.class))).thenReturn(6);
    when(applicationPreferences.getBatterySaferAtNightEnabled(context)).thenReturn(false);
    when(applicationPreferences.isDayProfile(eq(context), any(LocalTime.class))).thenReturn(true);

    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.getState()).thenReturn(OnOffState.OFF);

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    TelephonyManager telephonyManager = mock(TelephonyManager.class);
    when(telephonyManager.getCallState()).thenReturn(CALL_STATE_IDLE);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new LowSignalServiceWorkUnit(
        applicationPreferences,
        telephonyManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Intent intent = mock(Intent.class);
    when(intent.getIntExtra("FILTER_STATE", 0)).thenReturn(0);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

}