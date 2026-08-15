package com.github.frimtec.android.pikettassist.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService.Progress;

public class PikettService extends Service {

  private static final String ACTION_START = "START";
  private static final String ACTION_STOP = "STOP";
  private static final String EXTRA_PROGRESS_MAX = "PROGRESS_MAX";
  private static final String EXTRA_PROGRESS_VAL = "PROGRESS_VAL";

  public static void start(Context context, Progress progress) {
    Intent intent = new Intent(context, PikettService.class);
    intent.setAction(ACTION_START);
    if (progress != null) {
      intent.putExtra(EXTRA_PROGRESS_MAX, progress.getMax());
      intent.putExtra(EXTRA_PROGRESS_VAL, progress.getProgress());
    }
    context.startForegroundService(intent);
  }

  public static void stop(Context context) {
    Intent intent = new Intent(context, PikettService.class);
    intent.setAction(ACTION_STOP);
    context.startService(intent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && ACTION_START.equals(intent.getAction())) {
      Progress progress = null;
      if (intent.hasExtra(EXTRA_PROGRESS_MAX)) {
        progress = new Progress(intent.getIntExtra(EXTRA_PROGRESS_MAX, 0), intent.getIntExtra(EXTRA_PROGRESS_VAL, 0));
      }
      NotificationService notificationService = new NotificationService(this);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        startForeground(
            NotificationService.SHIFT_NOTIFICATION_ID,
            notificationService.createShiftOnNotification(progress),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        );
      } else {
        startForeground(
            NotificationService.SHIFT_NOTIFICATION_ID,
            notificationService.createShiftOnNotification(progress)
        );
      }
    } else if (intent == null || ACTION_STOP.equals(intent.getAction())) {
      stopForeground(STOP_FOREGROUND_REMOVE);
      stopSelf();
    }
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
