package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.PikettShift;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.helper.Feature;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.receiver.NotificationActionListener;
import com.github.frimtec.android.pikettassist.state.SharedState;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import java.util.Arrays;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_UPDATE_NOW;
import static com.github.frimtec.android.pikettassist.receiver.NotificationActionListener.EXTRA_DOWNLOAD_URL;
import static com.github.frimtec.android.pikettassist.receiver.NotificationActionListener.EXTRA_VERSION_NAME;

public class PikettService extends IntentService {

  private static final String TAG = "PikettService";
  private static final Duration MAX_SLEEP = Duration.ofHours(24);

  public PikettService() {
    super(TAG);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
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
    if (SharedState.getPikettState(this) == OnOffState.OFF && SharedState.checkForUpdates(this)) {
      versionCheck();
    }
    Instant now = PikettShift.now();
    Optional<PikettShift> first = CalendarEventHelper.getPikettShifts(this, SharedState.getCalendarEventPikettTitlePattern(this), SharedState.getCalendarSelection(this))
        .stream().filter(shift -> !shift.isOver(now)).findFirst();
    Instant nextRun = first.map(shift -> shift.isNow(now) ? shift.getEndTime(true) : shift.getStartTime(true)).orElse(now.plus(MAX_SLEEP).plusSeconds(10));
    long waitMs = Math.min(Duration.between(now, nextRun).toMillis(), MAX_SLEEP.toMillis());
    Log.i(TAG, "Next run in " + waitMs);
    AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

    if (first.map(PikettShift::isNow).orElse(false)) {
      NotificationHelper.notifyShiftOn(this);
      this.startService(new Intent(this, SignalStrengthService.class));
      this.startService(new Intent(this, TestAlertService.class));
    } else {
      NotificationHelper.cancelNotification(this, NotificationHelper.SHIFT_NOTIFICATION_ID);
    }
    alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + waitMs,
        PendingIntent.getService(this, 0, new Intent(this, PikettService.class), 0)
    );
  }

  private void versionCheck() {
    GitHubService.getInstance(this).loadLatestRelease(this, (context, latestRelease) -> {
      try {
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        String installedVersionName = packageInfo.versionName;
        String latestVersionName = latestRelease.getName();
        Log.i(TAG, String.format("Version check: installed version=%s; latest version=%s", installedVersionName, latestVersionName));
        if (installedVersionName.compareTo(latestVersionName) < 0) {
          Intent updateIntent = new Intent(context, NotificationActionListener.class);
          updateIntent.putExtra(EXTRA_VERSION_NAME, latestVersionName);
          updateIntent.putExtra(EXTRA_DOWNLOAD_URL, latestRelease.getApkUrl());
          updateIntent.setAction(ACTION_UPDATE_NOW);
          NotificationHelper.notifyNewVersion(context, latestVersionName, updateIntent);
        }
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, "Can not read version info", e);
      }
    });
  }
}
