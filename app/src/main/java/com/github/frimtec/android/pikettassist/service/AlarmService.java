package com.github.frimtec.android.pikettassist.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.activity.PikettAlarmActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.receiver.NotificationActionListener;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Instant;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CLOSE_ALARM;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_FALSE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_TRUE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_ALERT_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_MESSAGE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_CONFIRM_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_END_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_START_TIME;

public class AlarmService {

  private static final String TAG = "AlarmService";

  private final Context context;

  public AlarmService(Context context) {
    this.context = context;
  }

  public void newAlarm(Sms sms) {
    Pair<AlarmState, Long> alarmState = SharedState.getAlarmState();
    SharedState.setLastAlarmSmsNumberWithSubscriptionId(context, sms.getNumber(), sms.getSubscriptionId());
    try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
      Long alertId;
      if (alarmState.first == AlarmState.OFF) {
        Log.i(TAG, "Alarm state OFF -> ON");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ALERT_COLUMN_START_TIME, Instant.now().toEpochMilli());
        contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_FALSE);
        alertId = db.insert(TABLE_ALERT, null, contentValues);
        PikettAlarmActivity.trigger(sms.getNumber(), sms.getSubscriptionId(), context);
      } else if (alarmState.first == AlarmState.ON_CONFIRMED) {
        Log.i(TAG, "Alarm state ON_CONFIRMED -> ON");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_FALSE);
        alertId = alarmState.second;
        db.update(TABLE_ALERT, contentValues, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alertId)});
        PikettAlarmActivity.trigger(sms.getNumber(), sms.getSubscriptionId(), context);
      } else {
        Log.i(TAG, "Alarm state ON -> ON");
        alertId = alarmState.second;
      }
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_ALERT_CALL_COLUMN_ALERT_ID, alertId);
      contentValues.put(TABLE_ALERT_CALL_COLUMN_TIME, Instant.now().toEpochMilli());
      contentValues.put(TABLE_ALERT_CALL_COLUMN_MESSAGE, sms.getText());
      db.insert(TABLE_ALERT_CALL, null, contentValues);
    }
  }

  public void newManuallyAlarm(Instant startTime, String reason) {
    Pair<AlarmState, Long> alarmState = SharedState.getAlarmState();
    try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
      Long alertId;
      long startTimeEpochMillis = startTime.toEpochMilli();
      if (alarmState.first == AlarmState.OFF) {
        Log.i(TAG, "Alarm state OFF -> ON");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ALERT_COLUMN_START_TIME, startTimeEpochMillis);
        contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
        contentValues.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, startTimeEpochMillis);
        alertId = db.insert(TABLE_ALERT, null, contentValues);
      } else if (alarmState.first == AlarmState.ON_CONFIRMED) {
        Log.i(TAG, "Alarm state ON_CONFIRMED -> ON");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
        alertId = alarmState.second;
        db.update(TABLE_ALERT, contentValues, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alertId)});
      } else {
        Log.i(TAG, "Alarm state ON -> ON");
        alertId = alarmState.second;
      }
      ContentValues contentValues = new ContentValues();
      contentValues.put(TABLE_ALERT_CALL_COLUMN_ALERT_ID, alertId);
      contentValues.put(TABLE_ALERT_CALL_COLUMN_TIME, startTimeEpochMillis);
      contentValues.put(TABLE_ALERT_CALL_COLUMN_MESSAGE, String.format("%s: %s", context.getString(R.string.manually_created_alarm_reason_prefix), reason));
      db.insert(TABLE_ALERT_CALL, null, contentValues);
      NotificationHelper.notifyAlarm(
          context,
          new Intent(context, NotificationActionListener.class),
          ACTION_CLOSE_ALARM,
          context.getString(R.string.alert_action_close),
          new Intent(context, MainActivity.class)
      );
    }
  }

  public void confirmAlarm() {
    final Pair<String, Integer> smsNumberWithSubscriptionId = SharedState.getLastAlarmSmsNumberWithSubscriptionId(context);
    confirmAlarm(context, smsNumberWithSubscriptionId.first, smsNumberWithSubscriptionId.second);
  }

  public void confirmAlarm(Context context, String smsNumber, Integer subscriptionId) {
    try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
      try (Cursor cursor = writableDatabase.query(TABLE_ALERT,
          new String[]{TABLE_ALERT_COLUMN_CONFIRM_TIME},
          TABLE_ALERT_COLUMN_END_TIME + " IS NULL",
          null, null, null, null)) {
        ContentValues values = new ContentValues();
        if (!cursor.moveToFirst() || cursor.getLong(0) == 0) {
          values.put(TABLE_ALERT_COLUMN_CONFIRM_TIME, Instant.now().toEpochMilli());
        }
        values.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_TRUE);
        int update = writableDatabase.update(TABLE_ALERT, values,
            TABLE_ALERT_COLUMN_END_TIME + " IS NULL", null);
        if (update != 1) {
          Log.e(TAG, "One open case expected, but got " + update);
        }
      }
    }
    SmsHelper.confirmSms(context, SharedState.getSmsConfirmText(context), smsNumber, subscriptionId);
    NotificationHelper.notifyAlarm(
        context,
        new Intent(context, NotificationActionListener.class),
        ACTION_CLOSE_ALARM,
        context.getString(R.string.alert_action_close),
        new Intent(context, MainActivity.class)
    );
  }

  public void closeAlarm() {
    try (SQLiteDatabase writableDatabase = PAssist.getWritableDatabase()) {
      ContentValues values = new ContentValues();
      values.put("end_time", Instant.now().toEpochMilli());
      int update = writableDatabase.update(TABLE_ALERT, values, TABLE_ALERT_COLUMN_END_TIME + " is null", null);
      if (update != 1) {
        Log.e(TAG, "One open case expected, but got " + update);
      }
    }
    NotificationHelper.cancelNotification(context, NotificationHelper.ALERT_NOTIFICATION_ID);
    context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
  }
}
