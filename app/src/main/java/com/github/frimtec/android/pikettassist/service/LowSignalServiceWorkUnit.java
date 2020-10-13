package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

import java.util.Optional;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.isLowSignal;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;

final class LowSignalServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "LowSignalValidator";
  private static final int CHECK_INTERVAL_SECONDS = 60;
  public static final String EXTRA_FILTER_STATE = "FILTER_STATE";

  private final AlarmService alarmService;
  private final TelephonyManager telephonyManager;
  private final AlertDao alertDao;
  private final ShiftService shiftService;
  private final SignalStrengthService signalStrengthService;
  private final VolumeService volumeService;
  private final NotificationService notificationService;
  private final Context context;

  LowSignalServiceWorkUnit(
      AlarmService alarmService,
      TelephonyManager telephonyManager,
      AlertDao alertDao,
      ShiftService shiftService,
      SignalStrengthService signalStrengthService,
      VolumeService volumeService,
      NotificationService notificationService,
      Context context) {
    this.alarmService = alarmService;
    this.telephonyManager = telephonyManager;
    this.alertDao = alertDao;
    this.shiftService = shiftService;
    this.signalStrengthService = signalStrengthService;
    this.volumeService = volumeService;
    this.notificationService = notificationService;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    int currentFilterState = intent.getIntExtra(EXTRA_FILTER_STATE, 0);
    boolean pikettState = this.shiftService.getState() == OnOffState.ON;
    SignalLevel level = signalStrengthService.getSignalStrength();
    if (pikettState && ApplicationPreferences.getSuperviseSignalStrength(context) && isCallStateIdle() && !isAlarmStateOn() && isLowSignal(context, level)) {
      int lowSignalFilter = ApplicationPreferences.getLowSignalFilterSeconds(context);
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
      if (ApplicationPreferences.getNotifyLowSignal(context)) {
        notificationService.notifySignalLow(level);
      }
      LowSignalAlarmActivity.trigger(context, this.alarmService);
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
      if (ApplicationPreferences.getManageVolumeEnabled(context)) {
        volumeService.setVolume(ApplicationPreferences.getOnCallVolume(context, LocalTime.now()));
      }
      Intent intent = new Intent();
      Duration nextRunIn = Duration.ofSeconds(CHECK_INTERVAL_SECONDS);
      if (currentFilterState > 0) {
        intent.putExtra(EXTRA_FILTER_STATE, currentFilterState);
        if (currentFilterState <= ApplicationPreferences.getLowSignalFilterSeconds(context)) {
          nextRunIn = Duration.ofSeconds(PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR);
        }
      }
      return Optional.of(new ScheduleInfo(nextRunIn, intent));
    } else {
      return Optional.empty();
    }
  }
}
