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

public class PikettServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);

  private final ShiftService shiftService;
  private final Context context;

  public PikettServiceWorkUnit(ShiftService shiftService, Context context) {
    this.shiftService = shiftService;
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
    Duration prePostRunTime = ApplicationPreferences.getPrePostRunTime(context);
    Instant nextRun = first.map(shift -> shift.isNow(now, prePostRunTime) ? shift.getEndTime(prePostRunTime) : shift.getStartTime(prePostRunTime)).orElse(now.plus(MAX_SLEEP).plusSeconds(10));
    Duration waitTillNextRun = Duration.between(now, nextRun);

    boolean manageVolumeEnabled = ApplicationPreferences.getManageVolumeEnabled(context);
    VolumeService volumeService = manageVolumeEnabled ? new VolumeService(context) : null;
    NotificationService notificationService = new NotificationService(context);
    if (ApplicationState.getPikettStateManuallyOn() || first.map(shift -> shift.isNow(prePostRunTime)).orElse(false)) {
      notificationService.notifyShiftOn();
      if (manageVolumeEnabled) {
        int defaultVolume = ApplicationState.getDefaultVolume();
        if (defaultVolume == DEFAULT_VOLUME_NOT_SET) {
          ApplicationState.setDefaultVolume(volumeService.getVolume());
          volumeService.setVolume(ApplicationPreferences.getOnCallVolume(context, LocalTime.now()));
        }
      }
      LowSignalService.enqueueWork(context);
      TestAlarmService.enqueueWork(context);
    } else {
      notificationService.cancelNotification(NotificationService.SHIFT_NOTIFICATION_ID);
      if (manageVolumeEnabled) {
        int defaultVolume = ApplicationState.getDefaultVolume();
        if (defaultVolume != DEFAULT_VOLUME_NOT_SET) {
          volumeService.setVolume(defaultVolume);
          ApplicationState.setDefaultVolume(DEFAULT_VOLUME_NOT_SET);
        }
      }
    }
    return Optional.of(new ScheduleInfo(MAX_SLEEP.compareTo(waitTillNextRun) > 0 ? waitTillNextRun : MAX_SLEEP));
  }
}
