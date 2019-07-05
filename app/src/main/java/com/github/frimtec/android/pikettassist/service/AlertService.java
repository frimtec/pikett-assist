package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.receiver.AlarmActionListener;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE;
import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CONFIRM;

public class AlertService extends Service {

  private static final String TAG = "AlertService";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    Log.d(TAG, "Alert Service onStartCommand");

    Log.d(TAG, "Start ringtone.");
    Context context = getApplicationContext();
    Ringtone ringtone = RingtoneManager.getRingtone(context, getAlarmTone());
    ringtone.play();
    if (SharedState.getUseVibrate(context)) {
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    NotificationHelper.confirm(context, (dialogInterface, integer) -> {
      Log.d(TAG, "Confirm received.");
      confirmAlarm(context);
      ringtone.stop();
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      Log.d(TAG, "Alarm finished.");
    });
    stopSelf();
    return START_NOT_STICKY;
  }

  private void confirmAlarm(Context context) {
    try(SQLiteDatabase writableDatabase = PikettAssist.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put("confirm_time", Instant.now().toEpochMilli());
      int update = writableDatabase.update("t_alert", values, "end_time is null", null);
      if(update != 1) {
        Log.e(TAG, "One open case expected, but got " + update);
      }
    }
    SmsHelper.confimSms(SharedState.getSmsSenderNumber(context));
    NotificationHelper.notify(
        context,
        "Confirmed alarm, close when all activities are finsished.",
        new Intent(context, AlarmActionListener.class),
        ACTION_CLOSE,
        "CLOSE",
        new Intent(context, MainActivity.class)
    );
  }


  private Uri getAlarmTone() {
    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    if (alert == null) {
      // alert is null, using backup
      alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      // I can't see this ever being null (as always have a default notification)
      // but just incase
      if (alert == null) {
        // alert backup is null, using 2nd backup
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
      }
    }
    return alert;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
