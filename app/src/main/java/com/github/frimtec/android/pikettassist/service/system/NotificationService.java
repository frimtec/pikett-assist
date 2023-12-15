package com.github.frimtec.android.pikettassist.service.system;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.app.Notification.CATEGORY_ALARM;
import static android.app.Notification.CATEGORY_EVENT;
import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.app.NotificationManager.IMPORTANCE_MAX;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationManagerCompat;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.NotificationActionListener;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.ui.MainActivity;

import java.util.Set;


public class NotificationService {

  private static final String TAG = "NotificationService";

  public static final int ALERT_NOTIFICATION_ID = 1;
  public static final int SHIFT_NOTIFICATION_ID = 2;

  private static final int SIGNAL_NOTIFICATION_ID = 3;
  private static final int MISSING_TEST_ALARM_NOTIFICATION_ID = 4;
  public static final int BATTERY_NOTIFICATION_ID = 5;

  public static final String ACTION_CLOSE_ALARM = "com.github.frimtec.android.pikettassist.CLOSE_ALARM";
  public static final String ACTION_ON_CALL_NOTIFICATION_CLOSED_BY_USER = "com.github.frimtec.android.pikettassist.ON_CALL_NOTIFICATION_CLOSED_BY_USER";
  public static final String ACTION_LOW_BATTERY_NOTIFICATION_CLOSED_BY_USER = "com.github.frimtec.android.pikettassist.LOW_BATTERY_NOTIFICATION_CLOSED_BY_USER";

  private static final String CHANNEL_ID_ALARM = "com.github.frimtec.android.pikettassist.alarm";
  private static final String CHANNEL_ID_NOTIFICATION = "com.github.frimtec.android.pikettassist.notification";
  private static final String CHANNEL_ID_CHANGE_SYSTEM = "com.github.frimtec.android.pikettassist.changeSystem";
  public static final String ZEN_MODE = "zen_mode";

  private final Context context;

  public static class Progress {

    private final int max;
    private final int progress;

    public Progress(long max, long progress) {
      while (max > Integer.MAX_VALUE) {
        max = max / 10;
        progress = progress / 10;
      }
      this.max = (int) max;
      this.progress = (int) Math.min(Math.max(progress, 0), max);
    }

    public int getMax() {
      return max;
    }

    public int getProgress() {
      return progress;
    }
  }

  public NotificationService(Context context) {
    this.context = context;
  }

  public void registerChannel() {
    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    if (notificationManager != null) {
      createChannel(notificationManager, CHANNEL_ID_ALARM, context.getString(R.string.channel_name_alarm), context.getString(R.string.channel_description_alarm), IMPORTANCE_MAX);
      createChannel(notificationManager, CHANNEL_ID_NOTIFICATION, context.getString(R.string.channel_name_notification), context.getString(R.string.channel_description_notification), IMPORTANCE_DEFAULT);
      createChannel(notificationManager, CHANNEL_ID_CHANGE_SYSTEM, context.getString(R.string.channel_name_change_system), context.getString(R.string.channel_description_change_system), IMPORTANCE_LOW);
    }
  }

  private void createChannel(NotificationManager notificationManager, String channelId, String name, String description, int importance) {
    NotificationChannel channel = new NotificationChannel(channelId, name, importance);
    channel.setDescription(description);
    notificationManager.createNotificationChannel(channel);
  }

  public void notifyAlarm(Intent actionIntent, String action, String actionLabel, Intent notifyIntent) {
    actionIntent.setAction(action);
    PendingIntent confirmPendingIntent =
        PendingIntent.getBroadcast(context, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    String message = context.getString(R.string.notification_alert_text);
    Notification notification = new Builder(context, CHANNEL_ID_ALARM)
        .setContentTitle(context.getString(R.string.notification_alert_title))
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_siren_black)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .addAction(R.drawable.ic_siren, actionLabel, confirmPendingIntent)
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(notifyPendingIntent)
        .setOnlyAlertOnce(true)
        .build();
    notifyIfAllowed(context, ALERT_NOTIFICATION_ID, notification);
  }

  public void notifyMissingTestAlarm(Intent notifyIntent, Set<TestAlarmContext> testAlarmContexts) {
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    String message = context.getString(R.string.notification_missing_test_alert_text) + TextUtils.join(", ", testAlarmContexts);
    Notification notification = new Builder(context, CHANNEL_ID_ALARM)
        .setContentTitle(context.getString(R.string.notification_missing_test_alert_title))
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_test_alarm_black)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(notifyPendingIntent)
        .setOnlyAlertOnce(true)
        .build();
    notifyIfAllowed(context, MISSING_TEST_ALARM_NOTIFICATION_ID, notification);
  }

  public void notifyShiftOn() {
    notifyShiftOn(null);
  }

  public void notifyShiftOn(Progress progress) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    Builder notificationBuilder = new Builder(context, CHANNEL_ID_NOTIFICATION)
        .setContentTitle(context.getString(R.string.notification_pikett_on_title))
        .setContentText(context.getString(R.string.notification_pikett_on_text))
        .setSmallIcon(R.drawable.ic_eye_notification)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setOngoing(true)
        .setDeleteIntent(getDeleteIntent(ACTION_ON_CALL_NOTIFICATION_CLOSED_BY_USER));
    if (progress != null) {
      notificationBuilder.setProgress(progress.getMax(), progress.getProgress(), false);
    }
    notifyIfAllowed(context, SHIFT_NOTIFICATION_ID, notificationBuilder.build());
  }

  protected PendingIntent getDeleteIntent(String action) {
    Intent intent = new Intent(context, NotificationActionListener.class);
    intent.setAction(action);
    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
  }

  public void notifySignalLow(SignalLevel level) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    Notification notification = new Builder(context, CHANNEL_ID_NOTIFICATION)
        .setContentTitle(context.getString(R.string.notification_low_signal_title))
        .setContentText(String.format("%s: %s", context.getString(R.string.notification_low_signal_text), level.toString(context)))
        .setSmallIcon(R.drawable.ic_signal_cellular_black)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setAutoCancel(true)
        .build();
    notifyIfAllowed(context, SIGNAL_NOTIFICATION_ID, notification);
  }

  public void notifyBatteryLow(BatteryStatus batteryStatus) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    @SuppressLint("DefaultLocale")
    Notification notification = new Builder(context, CHANNEL_ID_ALARM)
        .setContentTitle(context.getString(R.string.notification_low_battery_title))
        .setContentText(String.format("%s: %d%%", context.getString(R.string.notification_low_battery_text), batteryStatus.level()))
        .setSmallIcon(R.drawable.ic_battery_alert_notification)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setOngoing(true)
        .setDeleteIntent(getDeleteIntent(ACTION_LOW_BATTERY_NOTIFICATION_CLOSED_BY_USER))
        .build();
    notifyIfAllowed(context, BATTERY_NOTIFICATION_ID, notification);
  }

  void notifyVolumeChanged(int oldLevel, int newLevel) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    String change = oldLevel > newLevel ? context.getString(R.string.reduced) : context.getString(R.string.increased);
    Notification notification = new Builder(context, CHANNEL_ID_CHANGE_SYSTEM)
        .setContentTitle(context.getString(R.string.notification_volume_changed_title) + " " + change)
        .setContentText(String.format(context.getString(R.string.notification_volume_changed_text), levelText(newLevel)))
        .setSmallIcon(R.drawable.ic_volume_notification)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setAutoCancel(true)
        .build();
    notifyIfAllowed(context, SIGNAL_NOTIFICATION_ID, notification);
  }

  public boolean isDoNotDisturbEnabled() {
    return getCurrentZenMode() > 0;
  }

  public int getCurrentZenMode() {
    try {
      return Settings.Global.getInt(this.context.getContentResolver(), ZEN_MODE);
    } catch (Settings.SettingNotFoundException e) {
      Log.e(TAG, "Zen mode setting not found", e);
      return -1;
    }
  }

  private static String levelText(int level) {
    return switch (level) {
      case 0 -> "0%";
      case 1 -> "15%";
      case 2 -> "30%";
      case 3 -> "45%";
      case 4 -> "60%";
      case 5 -> "75%";
      case 6 -> "90%";
      case 7 -> "100%";
      default -> level < 0 ? "0%" : "100%";
    };
  }

  public void cancelNotification(int id) {
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.cancel(id);
  }


  private void notifyIfAllowed(Context context, int id, @NonNull Notification notification) {
    if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
      NotificationManagerCompat.from(context).notify(id, notification);
    } else {
      Log.w(TAG, "Notification suppressed as permission is missing");
    }
  }

}
