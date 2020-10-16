package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;

import java.util.Optional;

import static com.github.frimtec.android.pikettassist.state.ApplicationState.DEFAULT_VOLUME_NOT_SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PikettServiceWorkUnitTest {

  private static final Duration ROUNDING_ERROR = Duration.ofSeconds(1);
  private static final Duration PRE_POST_RUN_TIME = Duration.ofMinutes(5);
  private static final Duration MAX_SLEEP = Duration.ofDays(1);
  private static final Duration SECURE_DELAY = Duration.ofSeconds(5);

  @Test
  void applyForNoShiftReturns24h() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.empty());
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForShiftMoreThan24hReturns24h() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(
        new Shift(
            1L,
            "Test",
            now.plus(Duration.ofDays(1).plus(PRE_POST_RUN_TIME).plus(Duration.ofMinutes(2))),
            now.plus(Duration.ofDays(2))))
    );
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForShiftMoreThan24hReturns24hWithManageVolumeEnabledSetsDefaultVolume() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(
        new Shift(
            1L,
            "Test",
            now.plus(Duration.ofDays(1).plus(PRE_POST_RUN_TIME).plus(Duration.ofMinutes(2))),
            now.plus(Duration.ofDays(2))))
    );
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(volumeService).setVolume(3);
    verify(applicationState).setDefaultVolume(DEFAULT_VOLUME_NOT_SET);
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForShiftMoreThan24hReturns24hWithManageVolumeDefNotSetEnabledNotSetsDefaultVolume() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(DEFAULT_VOLUME_NOT_SET);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(
        new Shift(
            1L,
            "Test",
            now.plus(Duration.ofDays(1).plus(PRE_POST_RUN_TIME).plus(Duration.ofMinutes(2))),
            now.plus(Duration.ofDays(2))))
    );
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForShiftLessThan24hReturnsTimeToStart() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(
        new Shift(
            1L,
            "Test",
            now.plus(Duration.ofDays(1).plus(PRE_POST_RUN_TIME).minus(Duration.ofMinutes(2))),
            now.plus(Duration.ofDays(2))))
    );
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    Duration expectedDelay = Duration.ofDays(1).minus(Duration.ofMinutes(2)).plus(SECURE_DELAY);
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isBetween(expectedDelay.minus(ROUNDING_ERROR), expectedDelay);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForShiftSwitchedOnShiftInBetween() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(new Shift(1L, "Test", now.minusSeconds(60), now.plusSeconds(60))));
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    Duration expectedDelay = PRE_POST_RUN_TIME.plusSeconds(60).plus(SECURE_DELAY);
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isBetween(expectedDelay.minus(ROUNDING_ERROR), expectedDelay);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger).run();
  }

  @Test
  void applyForShiftSwitchedOnShiftStarts() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(new Shift(1L, "Test", now.minus(PRE_POST_RUN_TIME), now.plusSeconds(60))));
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    Duration expectedDelay = PRE_POST_RUN_TIME.plusSeconds(60).plus(SECURE_DELAY);
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isBetween(expectedDelay.minus(ROUNDING_ERROR), expectedDelay);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger).run();
  }

  @Test
  void applyForShiftSwitchedOnShiftAlmostEnds() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(new Shift(1L, "Test", now.minus(Duration.ofMinutes(10)), now.minus(PRE_POST_RUN_TIME).plusSeconds(1))));
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    Duration expectedDelay = Duration.ofSeconds(1).plus(SECURE_DELAY);
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isBetween(expectedDelay.minus(ROUNDING_ERROR), expectedDelay);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger).run();
  }

  @Test
  void applyForShiftSwitchedOnShiftEndsReturnsRerunDelay() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(false);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    Instant now = Shift.now();
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.of(new Shift(1L, "Test", now.minus(Duration.ofMinutes(10)), now.minus(PRE_POST_RUN_TIME).minusMillis(1))));
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(Duration.ofMinutes(1));
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService, never()).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger, never()).run();
  }

  @Test
  void applyForManuallySwitchedOn() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(true);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(false);
    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.empty());
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger).run();
  }

  @Test
  void applyForManuallySwitchedOnWithManageVolumeEnabled() {
    // arrange
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(true);
    when(applicationState.getDefaultVolume()).thenReturn(3);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.empty());
    VolumeService volumeService = mock(VolumeService.class);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState, never()).setDefaultVolume(any(Integer.class));
    verify(volumeService, never()).setVolume(any(Integer.class));
    verify(jobTrigger).run();
  }

  @Test
  void applyForManuallySwitchedOnWithManageVolumeEnabledDefaultVolumeNotSet() {
    // arrange
    int newVolume = 7;
    int currentVolume = 5;
    Context context = mock(Context.class);
    NotificationService notificationService = mock(NotificationService.class);
    ApplicationState applicationState = mock(ApplicationState.class);
    when(applicationState.getPikettStateManuallyOn()).thenReturn(true);
    when(applicationState.getDefaultVolume()).thenReturn(DEFAULT_VOLUME_NOT_SET);
    ApplicationPreferences applicationPreferences = mock(ApplicationPreferences.class);
    when(applicationPreferences.getPrePostRunTime(context)).thenReturn(PRE_POST_RUN_TIME);
    when(applicationPreferences.getManageVolumeEnabled(context)).thenReturn(true);
    when(applicationPreferences.getOnCallVolume(eq(context), any(LocalTime.class))).thenReturn(newVolume);
    ShiftService shiftService = mock(ShiftService.class);
    when(shiftService.findCurrentOrNextShift(any(Instant.class))).thenReturn(Optional.empty());
    VolumeService volumeService = mock(VolumeService.class);
    when(volumeService.getVolume()).thenReturn(currentVolume);
    Runnable jobTrigger = mock(Runnable.class);

    ServiceWorkUnit workUnit = new PikettServiceWorkUnit(
        applicationState,
        applicationPreferences,
        shiftService,
        notificationService,
        volumeService,
        jobTrigger,
        context
    );

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(mock(Intent.class));

    // assert
    assertThat(scheduleInfo.orElseGet(() -> new ScheduleInfo(Duration.ZERO)).getScheduleDelay()).isEqualTo(MAX_SLEEP);
    verify(context).sendBroadcast(any(Intent.class));
    verify(notificationService, never()).cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
    verify(notificationService).notifyShiftOn();
    verify(applicationState).setDefaultVolume(currentVolume);
    verify(volumeService).setVolume(newVolume);
    verify(jobTrigger).run();
  }

}