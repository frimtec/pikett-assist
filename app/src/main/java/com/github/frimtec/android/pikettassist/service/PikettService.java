package com.github.frimtec.android.pikettassist.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.action.Action;
import com.github.frimtec.android.pikettassist.domain.Shift;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
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

public class PikettService extends IntentService {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);

  private ShiftService shiftService;
  private AlarmService alarmService;

  public PikettService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.shiftService = new ShiftService(this);
    this.alarmService = new AlarmService(this);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    sendBroadcast(new Intent(Action.REFRESH.getId()));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (Arrays.stream(Feature.values())
        .filter(Feature::isPermissionType)
        .anyMatch(set -> !set.isAllowed(this))) {
      Log.w(TAG, "Not all required permissions are granted. Services is stopped.");
      return;
    }
    Instant now = Shift.now();
    Optional<Shift> first = this.shiftService.findCurrentOrNextShift(now);
    Instant nextRun = first.map(shift -> shift.isNow(now) ? shift.getEndTime(true) : shift.getStartTime(true)).orElse(now.plus(MAX_SLEEP).plusSeconds(10));
    long waitMs = Math.min(Duration.between(now, nextRun).toMillis(), MAX_SLEEP.toMillis());
    Log.i(TAG, "Next run in " + waitMs);
    boolean manageVolumeEnabled = ApplicationPreferences.getManageVolumeEnabled(this);
    VolumeService volumeService = manageVolumeEnabled ? new VolumeService(this) : null;
    NotificationService notificationService = new NotificationService(this);
    if (ApplicationState.getPikettStateManuallyOn() || first.map(Shift::isNow).orElse(false)) {
      notificationService.notifyShiftOn();
      if (manageVolumeEnabled) {
        int defaultVolume = ApplicationState.getDefaultVolume();
        if (defaultVolume == DEFAULT_VOLUME_NOT_SET) {
          ApplicationState.setDefaultVolume(volumeService.getVolume());
          volumeService.setVolume(ApplicationPreferences.getOnCallVolume(this, LocalTime.now()));
        }
      }
      this.startService(new Intent(this, LowSignalService.class));
      this.startService(new Intent(this, TestAlarmService.class));
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
    this.alarmService.setAlarmRelative(waitMs, new Intent(this, PikettService.class));
  }
}
