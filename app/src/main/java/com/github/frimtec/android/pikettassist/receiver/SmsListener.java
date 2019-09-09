package com.github.frimtec.android.pikettassist.receiver;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.activity.PikettAlarmActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.domain.Sms;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.frimtec.android.pikettassist.helper.SmsHelper.confirmSms;
import static com.github.frimtec.android.pikettassist.state.DbHelper.BOOLEAN_FALSE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_ALERT_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_MESSAGE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_CALL_COLUMN_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_IS_CONFIRMED;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_ALERT_COLUMN_START_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
      if (SharedState.getPikettState(context) == OnOffState.OFF) {
        return;
      }
      long operationCenterContactId = SharedState.getAlarmOperationsCenterContact(context);
      for (Sms sms : SmsHelper.getSmsFromIntent(intent)) {
        Set<Long> contactIds = ContactHelper.lookupContactIdByPhoneNumber(context, sms.getNumber());
        if (operationCenterContactId != SharedState.EMPTY_CONTACT && contactIds.contains(operationCenterContactId)) {
          Log.i(TAG, "SMS from pikett number");
          Pattern testSmsPattern = Pattern.compile(SharedState.getSmsTestMessagePattern(context));
          Matcher matcher = testSmsPattern.matcher(sms.getText());
          if (matcher.matches()) {
            String id = matcher.groupCount() > 0 ? matcher.group(1) : null;
            id = id != null ? id : context.getString(R.string.test_alarm_context_general);
            Log.i(TAG, "TEST alarm with ID: " + id);
            confirmSms(SharedState.getSmsConfirmText(context), sms.getNumber());
            try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
              try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{id}, null, null, null)) {
                if (cursor.getCount() == 0) {
                  ContentValues contentValues = new ContentValues();
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_ID, id);
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, Instant.now().toEpochMilli());
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE, sms.getText());
                  db.insert(TABLE_TEST_ALERT_STATE, null, contentValues);
                } else {
                  ContentValues contentValues = new ContentValues();
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, Instant.now().toEpochMilli());
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE, sms.getText());
                  db.update(TABLE_TEST_ALERT_STATE, contentValues, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{id});
                }
              }
            }
          } else {
            Log.i(TAG, "Alarm");
            Pair<AlarmState, Long> alarmState = SharedState.getAlarmState();
            try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
              Long alertId;

              AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
              Objects.requireNonNull(alarmManager);
              Intent alarmIntent = new Intent(context, PikettAlarmActivity.class);
              alarmIntent.putExtra("sms_number", sms.getNumber());
              PendingIntent pendingIntent = PendingIntent.getActivity(context,
                  1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

              if (alarmState.first == AlarmState.OFF) {
                Log.i(TAG, "Alarm state OFF -> ON");
                ContentValues contentValues = new ContentValues();
                contentValues.put(TABLE_ALERT_COLUMN_START_TIME, Instant.now().toEpochMilli());
                contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_FALSE);
                alertId = db.insert(TABLE_ALERT, null, contentValues);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Instant.now().toEpochMilli() + 10, pendingIntent);
              } else if (alarmState.first == AlarmState.ON_CONFIRMED) {
                Log.i(TAG, "Alarm state ON_CONFIRMED -> ON");
                ContentValues contentValues = new ContentValues();
                contentValues.put(TABLE_ALERT_COLUMN_IS_CONFIRMED, BOOLEAN_FALSE);
                alertId = alarmState.second;
                db.update(TABLE_ALERT, contentValues, TABLE_ALERT_COLUMN_ID + "=?", new String[]{String.valueOf(alertId)});
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Instant.now().toEpochMilli() + 10, pendingIntent);
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
        }
      }
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    }
  }
}
