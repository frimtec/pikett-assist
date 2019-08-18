package com.github.frimtec.android.pikettassist.receiver;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;
import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.DualState;
import com.github.frimtec.android.pikettassist.domain.Sms;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.state.PikettAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.frimtec.android.pikettassist.helper.SmsHelper.confimSms;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";
  private SharedPreferences preferences;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      if (SharedState.getPikettState(context) == DualState.OFF) {
        Log.d(TAG, "SMS recived but not on pikett");
        return;
      }
      long operationCenterContactId = SharedState.getAlarmOperationsCenterContact(context);
      for (Sms sms : SmsHelper.getSmsFromIntent(intent)) {
        Optional<Long> contactId = ContactHelper.lookupContactIdByPhoneNumber(context, sms.getNumber());
        Log.d(TAG, "Contact ID: " + contactId);
        if (operationCenterContactId != SharedState.EMPTY_CONTACT && operationCenterContactId == contactId.orElse(SharedState.EMPTY_CONTACT)) {
          Log.d(TAG, "SMS from pikett number");
          Pattern testSmsPattern = Pattern.compile(SharedState.getSmsTestMessagePattern(context));
          Matcher matcher = testSmsPattern.matcher(sms.getText());
          if (matcher.matches()) {
            String id = matcher.groupCount() > 0 ? matcher.group(1) : null;
            id = id != null ? id : context.getString(R.string.test_alarm_context_general);
            Log.d(TAG, "TEST alarm with ID: " + id);
            confimSms(SharedState.getSmsConfirmText(context), sms.getNumber());
            try (SQLiteDatabase db = PikettAssist.getWritableDatabase()) {
              try (Cursor cursor = db.query("t_test_alarm_state", new String[]{"_id"}, "_id=?", new String[]{id}, null, null, null)) {
                if (cursor.getCount() == 0) {
                  ContentValues contentValues = new ContentValues();
                  contentValues.put("_id", id);
                  contentValues.put("last_received_time", Instant.now().toEpochMilli());
                  contentValues.put("message", sms.getText());
                  db.insert("t_test_alarm_state", null, contentValues);
                } else {
                  ContentValues contentValues = new ContentValues();
                  contentValues.put("last_received_time", Instant.now().toEpochMilli());
                  contentValues.put("message", sms.getText());
                  db.update("t_test_alarm_state", contentValues, "_id=?", new String[]{id});
                }
              }
            }
          } else {
            Log.d(TAG, "Alarm");
            Pair<AlarmState, Long> alarmState = SharedState.getAlarmState(context);
            try (SQLiteDatabase db = PikettAssist.getWritableDatabase()) {
              Long caseId;
              Intent alertServiceIntent = new Intent(context, AlertService.class);
              alertServiceIntent.putExtra("sms_number", sms.getNumber());
              if (alarmState.first == AlarmState.OFF) {
                Log.d(TAG, "Alarm state OFF -> ON");
                ContentValues contentValues = new ContentValues();
                contentValues.put("start_time", Instant.now().toEpochMilli());
                caseId = db.insert("t_alert", null, contentValues);
                context.startService(alertServiceIntent);
              } else if (alarmState.first == AlarmState.ON_CONFIRMED) {
                Log.d(TAG, "Alarm state ON_CONFIRMED -> ON");
                ContentValues contentValues = new ContentValues();
                contentValues.putNull("confirm_time");
                caseId = alarmState.second;
                db.update("t_alert", contentValues, "_id=?", new String[]{String.valueOf(caseId)});
                context.startService(alertServiceIntent);
              } else {
                Log.d(TAG, "Alarm state ON -> ON");
                caseId = alarmState.second;
              }
              ContentValues contentValues = new ContentValues();
              contentValues.put("case_id", caseId);
              contentValues.put("time", Instant.now().toEpochMilli());
              contentValues.put("time", Instant.now().toEpochMilli());
              contentValues.put("message", sms.getText());
              db.insert("t_alert_call", null, contentValues);
            }
          }
        }
      }
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    }
  }
}
