package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.Action;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.utility.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.utility.Feature;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;
import com.github.frimtec.android.pikettassist.utility.VolumeHelper;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;

import java.util.Arrays;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.state.SharedState.DEFAULT_VALUE_NOT_SET;

public class PikettService extends IntentService {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);

  public PikettService() {
    super(TAG);
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
    Instant now = PikettShift.now();
    Optional<PikettShift> first = CalendarEventHelper.getPikettShifts(this, SharedState.getCalendarEventPikettTitlePattern(this), SharedState.getCalendarSelection(this))
        .stream().filter(shift -> !shift.isOver(now)).findFirst();
    Instant nextRun = first.map(shift -> shift.isNow(now) ? shift.getEndTime(true) : shift.getStartTime(true)).orElse(now.plus(MAX_SLEEP).plusSeconds(10));
    long waitMs = Math.min(Duration.between(now, nextRun).toMillis(), MAX_SLEEP.toMillis());
    Log.i(TAG, "Next run in " + waitMs);
    AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

    boolean manageVolumeEnabled = SharedState.getManageVolumeEnabled(this);
    VolumeHelper volumeHelper = manageVolumeEnabled ? new VolumeHelper(this) : null;
    if (SharedState.getPikettStateManuallyOn(this) || first.map(PikettShift::isNow).orElse(false)) {
      NotificationHelper.notifyShiftOn(this);
      if (manageVolumeEnabled) {
        int defaultVolume = SharedState.getDefaultVolume(this);
        if (defaultVolume == DEFAULT_VALUE_NOT_SET) {
          SharedState.setDefaultVolume(this, volumeHelper.getVolume());
          volumeHelper.setVolume(SharedState.getOnCallVolume(this, LocalTime.now()));
        }
      }
      this.startService(new Intent(this, SignalStrengthService.class));
      this.startService(new Intent(this, TestAlarmService.class));
    } else {
      NotificationHelper.cancelNotification(this, NotificationHelper.SHIFT_NOTIFICATION_ID);
      if (manageVolumeEnabled) {
        int defaultVolume = SharedState.getDefaultVolume(this);
        if (defaultVolume != DEFAULT_VALUE_NOT_SET) {
          volumeHelper.setVolume(defaultVolume);
          SharedState.setDefaultVolume(this, DEFAULT_VALUE_NOT_SET);
        }
      }
    }
    alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + waitMs,
        PendingIntent.getService(this, 0, new Intent(this, PikettService.class), 0)
    );
  }
}
