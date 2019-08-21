package com.github.frimtec.android.pikettassist.helper;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper.SignalLevel;

import java.util.Set;
import java.util.function.BiConsumer;

import static android.app.Notification.CATEGORY_ALARM;
import static android.app.Notification.CATEGORY_EVENT;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;


public class NotificationHelper {
  private static final String TAG = "NotificationHelper";

  private static final String CHANNEL_ID = "com.github.frimtec.android.pikettassist";
  public static final int ALERT_NOTIFICATION_ID = 1;
  public static final int SHIFT_NOTIFICATION_ID = 2;
  public static final int SIGNAL_NOTIFICATION_ID = 3;
  public static final int MISSING_TEST_ALARM_NOTIFICATION_ID = 4;

  public static final String ACTION_CONFIRM_ALARM = "com.github.frimtec.android.pikettassist.CONFIRM_ALARM";
  public static final String ACTION_CLOSE_ALARM = "com.github.frimtec.android.pikettassist.CLOSE_ALARM";

  public static void registerChannel(Context context) {
    CharSequence name = context.getString(R.string.channel_name);
    String description = context.getString(R.string.channel_description);
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription(description);
    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);
  }

  public static void notify(Context context, Intent actionIntent, String action, String actionLabel, Intent notifyIntent) {
    actionIntent.setAction(action);
    PendingIntent confirmPendingIntent =
        PendingIntent.getBroadcast(context, 0, actionIntent, 0);

    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );

    String message = context.getString(R.string.notification_alert_text);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
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

  public static void notifyMissingTestAlarm(Context context, Intent notifyIntent, Set<String> testContexts) {
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );
    String message = context.getString(R.string.notification_missing_test_alert_text) + String.join(", ", testContexts);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
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

  public static void notifyShiftOn(Context context) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.notification_pikett_on_title))
        .setContentText(context.getString(R.string.notification_pikett_on_text))
        .setSmallIcon(R.drawable.ic_eye)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .build();
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SHIFT_NOTIFICATION_ID, notification);
  }

  public static void notifySignalLow(Context context, SignalLevel level) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(context.getString(R.string.notification_low_signal_title))
        .setContentText(String.format("%s: %s", context.getString(R.string.notification_low_signal_text), level.toString(context)))
        .setSmallIcon(R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp)
        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_large_icon))
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .build();

    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SIGNAL_NOTIFICATION_ID, notification);
  }

  public static void cancel(Context context, int id) {
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.cancel(id);
  }

  public static void confirm(Context context, BiConsumer<DialogInterface, Integer> action) {
    LayoutInflater factory = LayoutInflater.from(context);
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
    View view = factory.inflate(R.layout.alert_confirmation_dialog, null);
    AlertDialog dialog = alertDialogBuilder
        .setView(view)
        .setTitle(R.string.notification_alert_confirm_title)
        .setMessage(R.string.notification_alert_confirm_text)
        .setCancelable(false)
        .setPositiveButton(R.string.notification_alert_confirm_button, (alertDialog, id) -> {
              Log.d(TAG, "Alert confirmed!");
              action.accept(alertDialog, id);
            }
        ).create();
    Window window = dialog.getWindow();
    window.setType(TYPE_APPLICATION_OVERLAY | FLAG_KEEP_SCREEN_ON );

    dialog.show();
    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
    button.setBackgroundColor(context.getColor(R.color.confirmButtonBack));
    button.setTextColor(context.getColor(R.color.confirmButtonText));
    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24.0F);
  }

  public static void batteryOptimization(Context context, BiConsumer<DialogInterface, Integer> action) {
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        // set dialog message
        .setTitle(R.string.notification_battery_optimization_title)
        .setMessage(R.string.notification_battery_optimization_text)
        .setCancelable(true)
        .setPositiveButton("OK", action::accept
        ).create();
    alertDialog.getWindow().setType(TYPE_APPLICATION_OVERLAY);
    alertDialog.show();
  }
}
