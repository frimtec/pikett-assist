package com.github.frimtec.android.pikettassist.activity;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.receiver.AlarmActionListener;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Collections;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;

public class PikettAlarmActivity extends AbstractAlarmActivity {

  private static final String EXTRA_SMS_NUMBER = "sms_number";

  private static final String TAG = "PikettAlarmActivity";

  public PikettAlarmActivity() {
    super(TAG, R.string.notification_alert_title, Pair.create(400, 200), SwipeButtonStyle.RED);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String smsNumber = getIntent().getStringExtra(EXTRA_SMS_NUMBER);
    setSwipeAction(() -> confirmAlarm(this, smsNumber));
    setRingtone(RingtoneManager.getRingtone(this, getAlarmTone(this)));
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

  public static void trigger(String smsNumber, Context context, AlarmManager alarmManager) {
    AbstractAlarmActivity.trigger(PikettAlarmActivity.class, context, alarmManager, Collections.singletonList(Pair.create(EXTRA_SMS_NUMBER, smsNumber)));
  }

}