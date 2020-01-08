package com.github.frimtec.android.pikettassist.service.system;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.TestAlarmContext;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.ui.MainActivity;

import java.util.Set;

import static android.app.Notification.CATEGORY_ALARM;
import static android.app.Notification.CATEGORY_EVENT;
import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static android.app.NotificationManager.IMPORTANCE_LOW;
import static android.app.NotificationManager.IMPORTANCE_MAX;


public class NotificationService {

  public static final int ALERT_NOTIFICATION_ID = 1;
  public static final int SHIFT_NOTIFICATION_ID = 2;
  public static final int SIGNAL_NOTIFICATION_ID = 3;
  public static final int MISSING_TEST_ALARM_NOTIFICATION_ID = 4;

  public static final String ACTION_CLOSE_ALARM = "com.github.frimtec.android.pikettassist.CLOSE_ALARM";

  private static final String CHANNEL_ID_ALARM = "com.github.frimtec.android.pikettassist.alarm";
  private static final String CHANNEL_ID_NOTIFICATION = "com.github.frimtec.android.pikettassist.notification";
  private static final String CHANNEL_ID_CHANGE_SYSTEM = "com.github.frimtec.android.pikettassist.changeSystem";

  private final Context context;

  public NotificationService(Context context) {
    this.context = context;
  }

  public void registerChannel() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      if (notificationManager != null) {
        createChannel(notificationManager, CHANNEL_ID_ALARM, context.getString(R.string.channel_name_alarm), context.getString(R.string.channel_description_alarm), IMPORTANCE_MAX);
        createChannel(notificationManager, CHANNEL_ID_NOTIFICATION, context.getString(R.string.channel_name_notification), context.getString(R.string.channel_description_notification), IMPORTANCE_DEFAULT);
        createChannel(notificationManager, CHANNEL_ID_CHANGE_SYSTEM, context.getString(R.string.channel_name_change_system), context.getString(R.string.channel_description_change_system), IMPORTANCE_LOW);
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createChannel(NotificationManager notificationManager, String channelId, String name, String description, int importance) {
    NotificationChannel channel = new NotificationChannel(channelId, name, importance);
    channel.setDescription(description);
    notificationManager.createNotificationChannel(channel);
  }

  public void notifyAlarm(Intent actionIntent, String action, String actionLabel, Intent notifyIntent) {
    actionIntent.setAction(action);
    PendingIntent confirmPendingIntent =
        PendingIntent.getBroadcast(context, 0, actionIntent, 0);

    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );

    String message = context.getString(R.string.notification_alert_text);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALARM)
        .setContentTitle(context.getString(R.string.notification_alert_title))
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_siren)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .addAction(R.drawable.ic_siren, actionLabel, confirmPendingIntent)
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(notifyPendingIntent)
        .setOnlyAlertOnce(true)
        .build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(ALERT_NOTIFICATION_ID, notification);
  }

  public void notifyMissingTestAlarm(Intent notifyIntent, Set<TestAlarmContext> testAlarmContexts) {
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );
    String message = context.getString(R.string.notification_missing_test_alert_text) + TextUtils.join(", ", testAlarmContexts);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_ALARM)
        .setContentTitle(context.getString(R.string.notification_missing_test_alert_title))
        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_test_alarm)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(notifyPendingIntent)
        .setOnlyAlertOnce(true)
        .build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(MISSING_TEST_ALARM_NOTIFICATION_ID, notification);
  }

  public void notifyShiftOn() {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATION)
        .setContentTitle(context.getString(R.string.notification_pikett_on_title))
        .setContentText(context.getString(R.string.notification_pikett_on_text))
        .setSmallIcon(R.drawable.ic_eye)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setOngoing(true)
        .setContentIntent(notifyPendingIntent)
        .build();
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SHIFT_NOTIFICATION_ID, notification);
  }

  public void notifySignalLow(SignalLevel level) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATION)
        .setContentTitle(context.getString(R.string.notification_low_signal_title))
        .setContentText(String.format("%s: %s", context.getString(R.string.notification_low_signal_text), level.toString(context)))
        .setSmallIcon(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setAutoCancel(true)
        .build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SIGNAL_NOTIFICATION_ID, notification);
  }

  public void notifyVolumeChanged(int oldLevel, int newLevel) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );

    String change = oldLevel > newLevel ? context.getString(R.string.reduced) : context.getString(R.string.increased);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_CHANGE_SYSTEM)
        .setContentTitle(context.getString(R.string.notification_volume_changed_title) + " " + change)
        .setContentText(String.format(context.getString(R.string.notification_volume_changed_text), levelText(newLevel)))
        .setSmallIcon(R.drawable.ic_volume_up_black_24dp)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .setAutoCancel(true)
        .build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SIGNAL_NOTIFICATION_ID, notification);
  }

  private static String levelText(int level) {
    switch (level) {
      case 0:
        return "0%";
      case 1:
        return "15%";
      case 2:
        return "30%";
      case 3:
        return "45%";
      case 4:
        return "60%";
      case 5:
        return "75%";
      case 6:
        return "90%";
      case 7:
        return "100%";
      default:
        return level < 0 ? "0%" : "100%";
    }
  }

  public void cancelNotification(int id) {
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.cancel(id);
  }

}
