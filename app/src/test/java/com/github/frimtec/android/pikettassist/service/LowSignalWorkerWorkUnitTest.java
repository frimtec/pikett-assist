package com.github.frimtec.android.pikettassist.service;

import static android.media.AudioManager.MODE_IN_CALL;
import static android.media.AudioManager.MODE_NORMAL;
import static com.github.frimtec.android.pikettassist.service.LowSignalWorkUnit.EXTRA_FILTER_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.core.util.Pair;
import androidx.work.Data;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.ShiftState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

class LowSignalWorkerWorkUnitTest {

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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.OFF));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.OFF;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);

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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofMinutes(15));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 15)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(15));
    verify(resultIntent).putExtra(EXTRA_FILTER_STATE, 30);
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.MODERATE;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 15)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.OFF;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 15)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(15));
    verify(resultIntent).putExtra(EXTRA_FILTER_STATE, 15);
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 45)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent).putExtra(EXTRA_FILTER_STATE, 46);
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_IN_CALL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.ON, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.POOR;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.ON));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);
    ScheduleInfo info = scheduleInfo.orElse(new ScheduleInfo(Duration.ZERO));
    Intent resultIntent = mock(Intent.class);
    info.getIntentExtrasSetter().accept(resultIntent);

    // assert
    assertThat(info.getScheduleDelay()).isEqualTo(Duration.ofSeconds(90));
    verify(resultIntent, never()).putExtra(eq(EXTRA_FILTER_STATE), any(Integer.class));
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
    when(shiftService.getShiftState()).thenReturn(new ShiftState(OnOffState.OFF));

    SignalStrengthService signalStrengthService = mock(SignalStrengthService.class);
    SignalLevel signalLevel = SignalLevel.GREAT;
    when(signalStrengthService.getSignalStrength()).thenReturn(signalLevel);

    AudioManager audioManager = mock(AudioManager.class);
    when(audioManager.getMode()).thenReturn(MODE_NORMAL);

    AlertDao alertDao = mock(AlertDao.class);
    when(alertDao.getAlertState()).thenReturn(Pair.create(AlertState.OFF, 2L));

    NotificationService notificationService = mock(NotificationService.class);
    VolumeService volumeService = mock(VolumeService.class);
    Runnable alarmTrigger = mock(Runnable.class);

    WorkUnit workUnit = new LowSignalWorkUnit(
        applicationPreferences,
        audioManager,
        alertDao,
        shiftService,
        signalStrengthService,
        volumeService,
        notificationService,
        alarmTrigger,
        context
    );
    Data inputData = new Data.Builder()
        .putInt(EXTRA_FILTER_STATE, 0)
        .build();

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    verify(notificationService, never()).notifySignalLow(any(SignalLevel.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(alarmTrigger, never()).run();
  }

}