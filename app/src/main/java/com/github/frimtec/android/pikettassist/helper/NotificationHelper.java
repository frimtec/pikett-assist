package com.github.frimtec.android.pikettassist.helper;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.CellSignalStrength;
import android.telephony.SignalStrength;
import android.util.Log;
import android.view.WindowManager;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.activity.MainActivity;

import java.util.function.BiConsumer;

import static android.app.Notification.*;


public class NotificationHelper {
  private static final String TAG = "NotificationHelper";

  private static final String CHANNEL_ID = "com.github.frimtec.android.pikettassist";
  public  static final int ALERT_NOTIFICATION_ID = 1;
  public static final int SHIFT_NOTIFICATION_ID = 2;
  public static final int SIGNAL_NOTIFICATION_ID = 3;

  public static final String ACTION_CONFIRM = "com.github.frimtec.android.pikettassist.CONFIRM_ALARM";
  public static final String ACTION_CLOSE = "com.github.frimtec.android.pikettassist.CLOSE_ALARM";

  public static void registerChannel(Context context) {
    CharSequence name = context.getString(R.string.channel_name);
    String description = context.getString(R.string.channel_description);
    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription(description);
    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(channel);
  }

  public static void notify(Context context, String text, Intent actionIntent, String action, String actionLabel, Intent notifyIntent) {
    actionIntent.setAction(action);
    PendingIntent confirmPendingIntent =
        PendingIntent.getBroadcast(context, 0, actionIntent, 0);

    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
    );

    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Pikett ALARM")
        .setContentText(text)
        // TODO choose icon for confirm and close
        .setSmallIcon(R.drawable.pikett_alarm)
        .addAction(R.drawable.pikett_alarm, actionLabel, confirmPendingIntent)
        .setCategory(CATEGORY_ALARM)
        .setContentIntent(notifyPendingIntent)
        .setOnlyAlertOnce(true)
        .build();
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(ALERT_NOTIFICATION_ID, notification);
  }

  public static void notifyShiftOn(Context context) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Pikett ON")
        .setContentText("You are on Pikett!")
        // TODO choose icon for confirm and close
        .setSmallIcon(R.drawable.pikett_alarm)
        .setCategory(CATEGORY_EVENT)
        .setOnlyAlertOnce(true)
        .setContentIntent(notifyPendingIntent)
        .build();
    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
    notificationManagerCompat.notify(SHIFT_NOTIFICATION_ID, notification);
  }

  public static void notifySignalLow(Context context, CellSignalStrength signalStrength) {
    PendingIntent notifyPendingIntent = PendingIntent.getActivity(
        context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
    );
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle("Low signal")
        .setContentText("Signal level: " + (signalStrength != null ? signalStrength.getLevel() : "OFF"))
        // TODO choose icon for confirm and close
        .setSmallIcon(R.drawable.pikett_alarm)
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
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        // set dialog message
        .setTitle("Pikett alarm received")
        .setMessage("Please confirm!")
        .setCancelable(false)
        .setPositiveButton("CONFIRM", (dialog, id) -> {
              // if this button is clicked, close
              // current activity
              Log.d(TAG, "Alert confirmed!");
              action.accept(dialog, id);
            }
        ).create();
    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
    alertDialog.show();
  }
}
