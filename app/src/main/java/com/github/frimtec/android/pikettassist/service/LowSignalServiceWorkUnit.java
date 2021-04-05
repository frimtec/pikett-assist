package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.BatteryService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

import java.util.Optional;
import java.util.function.Consumer;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static com.github.frimtec.android.pikettassist.service.system.NotificationService.BATTERY_NOTIFICATION_ID;
import static com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.isLowSignal;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;

final class LowSignalServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "LowSignalValidator";

  private static final String EXTRA_FILTER_STATE = "FILTER_STATE";
  private static final Duration CHECK_INTERVAL = Duration.ofSeconds(90);
  private static final Duration CHECK_INTERVAL_BATTERY_SAFER = Duration.ofMinutes(15);
  private static final int BATTERY_LOW_LIMIT = 10;

  private final ApplicationPreferences applicationPreferences;
  private final TelephonyManager telephonyManager;
  private final AlertDao alertDao;
  private final ShiftService shiftService;
  private final SignalStrengthService signalStrengthService;
  private final VolumeService volumeService;
  private final NotificationService notificationService;
  private final Runnable alarmTrigger;
  private final Context context;

  LowSignalServiceWorkUnit(
      ApplicationPreferences applicationPreferences,
      TelephonyManager telephonyManager,
      AlertDao alertDao,
      ShiftService shiftService,
      SignalStrengthService signalStrengthService,
      VolumeService volumeService,
      NotificationService notificationService,
      Runnable alarmTrigger,
      Context context) {
    this.applicationPreferences = applicationPreferences;
    this.telephonyManager = telephonyManager;
    this.alertDao = alertDao;
    this.shiftService = shiftService;
    this.signalStrengthService = signalStrengthService;
    this.volumeService = volumeService;
    this.notificationService = notificationService;
    this.alarmTrigger = alarmTrigger;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    int currentFilterState = intent.getIntExtra(EXTRA_FILTER_STATE, 0);
    boolean pikettState = this.shiftService.getState() == OnOffState.ON;
    SignalLevel level = this.signalStrengthService.getSignalStrength();
    if (pikettState && this.applicationPreferences.getSuperviseSignalStrength(context) && isCallStateIdle() && !isAlarmStateOn() && isLowSignal(level, this.applicationPreferences.getSuperviseSignalStrengthMinLevel(context))) {
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
        this.notificationService.notifySignalLow(level);
      }
      this.alarmTrigger.run();
    } else {
      if (currentFilterState > 0) {
        Log.d(TAG, "Filter stopped, signal ok");
        currentFilterState = 0;
      }
    }
    return reSchedule(currentFilterState, pikettState);
  }

  private boolean isAlarmStateOn() {
    return this.alertDao.getAlertState().first == AlertState.ON;
  }

  private boolean isCallStateIdle() {
    return this.telephonyManager.getCallState() == CALL_STATE_IDLE;
  }

  private Optional<ScheduleInfo> reSchedule(int currentFilterState, boolean pikettState) {
    if (pikettState) {
      LocalTime now = LocalTime.now();
      if (this.applicationPreferences.getManageVolumeEnabled(context)) {
        volumeService.setVolume(this.applicationPreferences.getOnCallVolume(context, now));
      }
      BatteryStatus batteryStatus = new BatteryService(context).batteryStatus();
      if(this.applicationPreferences.getSuperviseBatteryLevel(context)) {
        if(batteryStatus.getLevel() <= this.applicationPreferences.getBatteryWarnLevel(context)) {
          notificationService.notifyBatteryLow(batteryStatus);
        } else {
          notificationService.cancelNotification(BATTERY_NOTIFICATION_ID);
        }
      } else {
        notificationService.cancelNotification(BATTERY_NOTIFICATION_ID);
      }
      Consumer<Intent> intentExtrasSetter;
      Duration nextRunIn = isBatterySaferOn(now) || batteryStatus.getLevel() < BATTERY_LOW_LIMIT ? getBatterySaferInterval(now) : CHECK_INTERVAL;
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
      notificationService.cancelNotification(BATTERY_NOTIFICATION_ID);
      return Optional.empty();
    }
  }

  private Duration getBatterySaferInterval(LocalTime now) {
    return this.applicationPreferences.isDayProfile(context, now.plus(CHECK_INTERVAL_BATTERY_SAFER)) ? CHECK_INTERVAL : CHECK_INTERVAL_BATTERY_SAFER;
  }

  private boolean isBatterySaferOn(LocalTime now) {
    return this.applicationPreferences.getBatterySaferAtNightEnabled(this.context) && !this.applicationPreferences.isDayProfile(context, now);
  }
}
