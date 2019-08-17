package com.github.frimtec.android.pikettassist.service;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.receiver.AlarmActionListener;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE;

public class AlertService extends Service {

  private static final String TAG = "AlertService";

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    String smsNumber = intent.getStringExtra("sms_number");
    Log.d(TAG, "Service cycle: " + smsNumber);

    Context context = getApplicationContext();
    Ringtone ringtone = RingtoneManager.getRingtone(context, getAlarmTone(context));
    ringtone.play();
    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    long[] pattern = {0, 400, 200};
    vibrator.vibrate(pattern, 0);

    NotificationHelper.confirm(context, (dialogInterface, integer) -> {
      Log.d(TAG, "Confirm received.");
      confirmAlarm(context, smsNumber);
      ringtone.stop();
      vibrator.cancel();
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      Log.d(TAG, "Alarm finished.");
    });
    stopSelf();
    return START_NOT_STICKY;
  }

  private void confirmAlarm(Context context, String smsNumber) {
    try (SQLiteDatabase writableDatabase = PikettAssist.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put("confirm_time", Instant.now().toEpochMilli());
      int update = writableDatabase.update("t_alert", values, "end_time is null", null);
      if (update != 1) {
        Log.e(TAG, "One open case expected, but got " + update);
      }
    }
    SmsHelper.confimSms(SharedState.getSmsConfirmText(context), smsNumber);
    NotificationHelper.notify(
        context,
        new Intent(context, AlarmActionListener.class),
        ACTION_CLOSE,
        getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = SharedState.getAlarmRingTone(context);
    if(!alarmRingTone.isEmpty()) {
      Log.d(TAG, "Use configured ringtone: " + alarmRingTone);
      return Uri.parse(alarmRingTone);
    }
    Log.d(TAG, "Use default ringtone");
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
