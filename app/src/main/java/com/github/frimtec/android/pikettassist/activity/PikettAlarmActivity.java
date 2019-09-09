package com.github.frimtec.android.pikettassist.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.helper.VibrateHelper;
import com.github.frimtec.android.pikettassist.receiver.AlarmActionListener;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import in.shadowfax.proswipebutton.ProSwipeButton;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;

public class PikettAlarmActivity extends AppCompatActivity {

  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private PowerManager.WakeLock wakeLock;
  private Vibrator vibrate;
  private Timer timer;
  private Ringtone ringtone;

  private static final String TAG = "PikettAlarmActivity";


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String smsNumber = getIntent().getStringExtra("sms_number");

    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    Objects.requireNonNull(pm);
    wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PikettAlarmActivity:alarm");
    Objects.requireNonNull(wakeLock);
    wakeLock.acquire(0);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    setContentView(R.layout.alarm);

    TextView textView = findViewById(R.id.alarm_text);
    textView.setText(R.string.notification_alert_title);

    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.hide();
    }

    stopped.set(false);

    this.timer = new Timer();
    this.ringtone = RingtoneManager.getRingtone(this, getAlarmTone(this));

    ProSwipeButton swipeButton = findViewById(R.id.alarm_button_confirm);
    swipeButton.setBackgroundColor(getColor(R.color.confirmButtonBackRed));
    swipeButton.setTextColor(getColor(R.color.confirmButtonTextRed));
    swipeButton.setArrowColor(getColor(R.color.confirmButtonArrowRed));
    swipeButton.setOnSwipeListener(() -> {
      swipeButton.showResultIcon(true);
      confirmAlarm(this, smsNumber);
      stopped.set(true);
      finish();
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    this.ringtone.play();
    this.timer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if (!PikettAlarmActivity.this.ringtone.isPlaying()) {
          PikettAlarmActivity.this.ringtone.play();
        }
      }
    }, 1000, 1000);
    this.vibrate = VibrateHelper.vibrate(this, 400, 200);
  }

  @Override
  protected void onStop() {
    super.onStop();
    this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    if (stopped.get()) {
      super.onStop();
      timer.cancel();
      vibrate.cancel();
      ringtone.stop();
      wakeLock.release();
    }
  }

  private void confirmAlarm(Context context, String smsNumber) {
    Log.v(TAG, "Confirm Alarm");
    try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
      try (Cursor cursor = writableDatabase.query(TABLE_ALERT, new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME}, TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null, null, null, null)) {
        ContentValues values = new ContentValues();
        if (!cursor.moveToFirst() || cursor.getLong(0) == 0) {
          values.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, Instant.now().toEpochMilli());
        }
        values.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
        int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null);
        if (update != 1) {
          Log.e(TAG, "One open case expected, but got " + update);
        }
      }
    }
    SmsHelper.confirmSms(SharedState.getSmsConfirmText(context), smsNumber);
    NotificationHelper.notifyAlarm(
        context,
        new Intent(context, AlarmActionListener.class),
        ACTION_CLOSE_ALARM,
        getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = SharedState.getAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

}