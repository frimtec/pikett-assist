package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.system.NotificationService.BATTERY_NOTIFICATION_ID;
import static com.github.frimtec.android.pikettassist.service.system.NotificationService.SHIFT_NOTIFICATION_ID;
import static com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.isLowSignal;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import androidx.work.Data;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.BatteryService;
import com.github.frimtec.android.pikettassist.service.system.InternetAvailabilityService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

final class LowSignalWorkUnit implements WorkUnit {

  private static final String TAG = "LowSignalValidator";

  static final String EXTRA_FILTER_STATE = "FILTER_STATE";
  private static final Duration CHECK_INTERVAL = Duration.ofSeconds(90);
  private static final Duration CHECK_INTERVAL_BATTERY_SAFER = Duration.ofMinutes(15);
  private static final int BATTERY_LOW_LIMIT = 10;

  private static final AtomicInteger CALL_COUNTER = new AtomicInteger(0);

  private final ApplicationPreferences applicationPreferences;
  private final AudioManager audioManager;
  private final AlertDao alertDao;
  private final ShiftService shiftService;
  private final SignalStrengthService signalStrengthService;
  private final InternetAvailabilityService internetAvailabilityService;
  private final VolumeService volumeService;
  private final NotificationService notificationService;
  private final Runnable alarmTrigger;
  private final Context context;

  LowSignalWorkUnit(
      ApplicationPreferences applicationPreferences,
      AudioManager audioManager,
      AlertDao alertDao,
      ShiftService shiftService,
      SignalStrengthService signalStrengthService,
      InternetAvailabilityService internetAvailabilityService,
      VolumeService volumeService,
      NotificationService notificationService,
      Runnable alarmTrigger,
      Context context) {
    this.applicationPreferences = applicationPreferences;
    this.audioManager = audioManager;
    this.alertDao = alertDao;
    this.shiftService = shiftService;
    this.signalStrengthService = signalStrengthService;
    this.internetAvailabilityService = internetAvailabilityService;
    this.volumeService = volumeService;
    this.notificationService = notificationService;
    this.alarmTrigger = alarmTrigger;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Data inputData) {
    int currentFilterState = inputData.getInt(EXTRA_FILTER_STATE, 0);
    boolean pikettState = this.shiftService.getShiftState().isOn();
    SignalLevel level = this.signalStrengthService.getSignalStrength();
    if (pikettState &&
        this.applicationPreferences.getSuperviseSignalStrength(context) &&
        !isInCall() &&
        !isAlarmStateOn()) {
      boolean lowSignal = isLowSignal(level, this.applicationPreferences.getSuperviseSignalStrengthMinLevel(context));
      boolean noInternet = this.applicationPreferences.getAlertConfirmMethod(context).isInternet() && isNoInternet();
      if (lowSignal || noInternet) {
        int lowSignalFilter = this.applicationPreferences.getLowSignalFilterSeconds(context);
        if (lowSignalFilter > 0 && level != SignalLevel.OFF) {
          if (currentFilterState < lowSignalFilter) {
            Log.d(TAG, "Filter round: " + currentFilterState);
            currentFilterState += PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;
            return reSchedule(currentFilterState, true);
          } else {
            currentFilterState = lowSignalFilter + 1;
            Log.d(TAG, "Filter triggered, alarm raced");
          }
        }
        if (this.applicationPreferences.getNotifyLowSignal(context)) {
          if (lowSignal) {
            this.notificationService.notifySignalLow(level);
          } else {
            this.notificationService.notifyNoInternet();
          }
        }
        this.alarmTrigger.run();
      } else {
        if (currentFilterState > 0) {
          Log.d(TAG, "Filter stopped, signal ok");
          currentFilterState = 0;
        }
      }
    }
    return reSchedule(currentFilterState, pikettState);
  }

  private boolean isNoInternet() {
    return !this.internetAvailabilityService.getInternetAvailability().isAvailable();
  }

  private boolean isAlarmStateOn() {
    return this.alertDao.getAlertState().first == AlertState.ON;
  }

  private boolean isInCall() {
    return audioManager.getMode() == AudioManager.MODE_IN_CALL;
  }

  private Optional<ScheduleInfo> reSchedule(int currentFilterState, boolean pikettState) {
    int callCount = CALL_COUNTER.getAndIncrement();
    if (pikettState) {
      LocalTime now = LocalTime.now();
      if (this.applicationPreferences.getManageVolumeEnabled(context)) {
        volumeService.setVolume(this.applicationPreferences.getOnCallVolume(context, now));
      }
      BatteryStatus batteryStatus = new BatteryService(context).batteryStatus();
      if (this.applicationPreferences.getSuperviseBatteryLevel(context)) {
        if (batteryStatus.level() <= this.applicationPreferences.getBatteryWarnLevel(context)) {
          notificationService.notifyBatteryLow(batteryStatus);
        } else {
          notificationService.cancelNotification(BATTERY_NOTIFICATION_ID);
        }
      } else {
        notificationService.cancelNotification(BATTERY_NOTIFICATION_ID);
      }
      if (callCount % 15 == 0) {
        updateShiftProgress();
      }
      Consumer<Intent> intentExtrasSetter;
      Duration nextRunIn = isBatterySaferOn(now) || batteryStatus.level() < BATTERY_LOW_LIMIT ? getBatterySaferInterval(now) : CHECK_INTERVAL;
      if (currentFilterState > 0) {
        intentExtrasSetter = intent -> intent.putExtra(EXTRA_FILTER_STATE, currentFilterState);
        if (currentFilterState <= this.applicationPreferences.getLowSignalFilterSeconds(context)) {
          nextRunIn = Duration.ofSeconds(PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR);
        }
      } else {
        intentExtrasSetter = intent -> {
        };
      }
      return Optional.of(new ScheduleInfo(nextRunIn, intentExtrasSetter));
    } else {
      Stream.of(
          BATTERY_NOTIFICATION_ID,
          SHIFT_NOTIFICATION_ID
      ).forEach(notificationService::cancelNotification);
      return Optional.empty();
    }
  }

  private void updateShiftProgress() {
    Instant nowInstant = Shift.now();
    Optional<Shift> optionalShift = this.shiftService.findCurrentOrNextShift(nowInstant);
    optionalShift.ifPresent(shift -> {
      if (shift.isNow(nowInstant, Duration.ZERO)) {
        Log.d(TAG, "Update shift progress");
        notificationService.notifyShiftOn(calculateProgress(shift, nowInstant));
      }
    });
  }

  private Duration getBatterySaferInterval(LocalTime now) {
    return this.applicationPreferences.isDayProfile(context, now.plus(CHECK_INTERVAL_BATTERY_SAFER)) ? CHECK_INTERVAL : CHECK_INTERVAL_BATTERY_SAFER;
  }

  private boolean isBatterySaferOn(LocalTime now) {
    return this.applicationPreferences.getBatterySaferAtNightEnabled(this.context) && !this.applicationPreferences.isDayProfile(context, now);
  }

  private NotificationService.Progress calculateProgress(Shift shift, Instant now) {
    long startTime = shift.getStartTime().getEpochSecond();
    return new NotificationService.Progress(
        shift.getEndTime().getEpochSecond() - startTime,
        now.getEpochSecond() - startTime
    );
  }

}
