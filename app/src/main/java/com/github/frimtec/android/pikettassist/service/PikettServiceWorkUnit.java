package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.service.system.Feature;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;

import java.util.Arrays;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.state.ApplicationState.DEFAULT_VOLUME_NOT_SET;

class PikettServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);
  private static final Duration SECURE_DELAY = Duration.ofSeconds(5);
  private static final Duration RERUN_DELAY = Duration.ofMinutes(1);

  private final ApplicationState applicationState;
  private final ApplicationPreferences applicationPreferences;
  private final ShiftService shiftService;
  private final NotificationService notificationService;
  private final VolumeService volumeService;
  private final Runnable jobTrigger;
  private final Context context;

  public PikettServiceWorkUnit(
      ApplicationState applicationState,
      ApplicationPreferences applicationPreferences,
      ShiftService shiftService,
      NotificationService notificationService,
      VolumeService volumeService,
      Runnable jobTrigger,
      Context context) {
    this.applicationState = applicationState;
    this.applicationPreferences = applicationPreferences;
    this.shiftService = shiftService;
    this.notificationService = notificationService;
    this.volumeService = volumeService;
    this.jobTrigger = jobTrigger;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    context.sendBroadcast(new Intent(Action.REFRESH.getId()));
    if (Arrays.stream(Feature.values())
        .filter(Feature::isPermissionType)
        .anyMatch(set -> !set.isAllowed(context))) {
      Log.w(TAG, "Not all required permissions are granted. Services is stopped.");
      return Optional.empty();
    }
    Instant now = Shift.now();
    Optional<Shift> first = this.shiftService.findCurrentOrNextShift(now);
    Duration prePostRunTime = this.applicationPreferences.getPrePostRunTime(context);
    Instant nextRun = first.map(shift -> shift.isNow(now, prePostRunTime) ? shift.getEndTime(prePostRunTime) : shift.getStartTime(prePostRunTime)).orElse(now.plus(MAX_SLEEP)).plus(SECURE_DELAY);
    Duration waitTillNextRun = Duration.between(now, nextRun);
    if(waitTillNextRun.isNegative()) {
      waitTillNextRun = RERUN_DELAY;
    }

    boolean manageVolumeEnabled = this.applicationPreferences.getManageVolumeEnabled(context);
    if (this.applicationState.getPikettStateManuallyOn() || first.map(shift -> shift.isNow(prePostRunTime)).orElse(false)) {
      this.notificationService.notifyShiftOn();
      if (manageVolumeEnabled) {
        int defaultVolume = this.applicationState.getDefaultVolume();
        if (defaultVolume == DEFAULT_VOLUME_NOT_SET) {
          this.applicationState.setDefaultVolume(this.volumeService.getVolume());
          this.volumeService.setVolume(this.applicationPreferences.getOnCallVolume(context, LocalTime.now()));
        }
      }
      jobTrigger.run();
    } else {
      this.notificationService.cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
      if (manageVolumeEnabled) {
        int defaultVolume = this.applicationState.getDefaultVolume();
        if (defaultVolume != DEFAULT_VOLUME_NOT_SET) {
          this.volumeService.setVolume(defaultVolume);
          this.applicationState.setDefaultVolume(DEFAULT_VOLUME_NOT_SET);
        }
      }
    }
    return Optional.of(new ScheduleInfo(MAX_SLEEP.compareTo(waitTillNextRun) > 0 ? waitTillNextRun : MAX_SLEEP));
  }
}
