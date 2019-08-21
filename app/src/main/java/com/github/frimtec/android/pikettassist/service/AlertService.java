package com.github.frimtec.android.pikettassist.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.state.DbHelper.*;

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

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if (!ringtone.isPlaying()) {
          Log.v(TAG, "Restart ringtone");
          ringtone.play();
        }
      }
    }, 1000*1, 1000*1);
    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    long[] pattern = {0, 400, 200};
    vibrator.vibrate(pattern, 0);

    NotificationHelper.confirm(context, (dialogInterface, integer) -> {
      Log.d(TAG, "Confirm received.");
      confirmAlarm(context, smsNumber);
      timer.cancel();
      ringtone.stop();
      vibrator.cancel();
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      Log.d(TAG, "Alarm finished.");
    });
    stopSelf();
    return START_NOT_STICKY;
  }

  private void confirmAlarm(Context context, String smsNumber) {
    try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
      try(Cursor cursor = writableDatabase.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null, null, null, null)) {
        ContentValues values = new ContentValues();
        if(!cursor.moveToFirst() || cursor.getLong(0) == 0) {
          values.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, Instant.now().toEpochMilli());
        }
        values.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
        int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null);
        if (update != 1) {
          Log.e(TAG, "One open case expected, but got " + update);
        }
      }
    }
    SmsHelper.confimSms(SharedState.getSmsConfirmText(context), smsNumber);
    NotificationHelper.notify(
        context,
        new Intent(context, AlarmActionListener.class),
            ACTION_CLOSE_ALARM,
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
