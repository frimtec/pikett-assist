package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.ContactHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;
import com.github.frimtec.android.pikettassist.service.AlarmService;
import com.github.frimtec.android.pikettassist.state.PAssist;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;
import com.github.frimtec.android.securesmsproxyapi.Sms;

import org.threeten.bp.Instant;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.frimtec.android.pikettassist.helper.SmsHelper.confirmSms;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_ID;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME;
import static com.github.frimtec.android.pikettassist.state.DbHelper.TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";

  @Override
  public void onReceive(Context context, Intent intent) {
    if ("com.github.frimtec.android.securesmsproxy.SMS_RECEIVED".equals(intent.getAction())) {
      Log.d(TAG, "SMS received");
      List<Sms> receivedSms = SmsHelper.getSmsFromIntent(context, intent);
      receivedSms.stream()
          .filter(sms -> SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK.equals(sms.getNumber()))
          .forEach(sms -> Toast.makeText(context, context.getString(R.string.sms_listener_loopback_sms_received), Toast.LENGTH_SHORT).show());
      if (SharedState.getPikettState(context) == OnOffState.OFF) {
        Log.d(TAG, "Drop SMS, not on-call");
        return;
      }
      long operationCenterContactId = SharedState.getAlarmOperationsCenterContact(context);
      for (Sms sms : receivedSms) {
        Set<Long> contactIds = ContactHelper.lookupContactIdByPhoneNumber(context, sms.getNumber());
        if (operationCenterContactId != SharedState.EMPTY_CONTACT && contactIds.contains(operationCenterContactId)) {
          Log.i(TAG, "SMS from pikett number");
          Pattern testSmsPattern = Pattern.compile(SharedState.getSmsTestMessagePattern(context), Pattern.DOTALL);
          Matcher matcher = testSmsPattern.matcher(sms.getText());
          if (SharedState.getTestAlarmEnabled(context) && matcher.matches()) {
            String id = matcher.groupCount() > 0 ? matcher.group(1) : null;
            id = id != null ? id : context.getString(R.string.test_alarm_context_general);
            Log.i(TAG, "TEST alarm with ID: " + id);
            confirmSms(context, SharedState.getSmsConfirmText(context), sms.getNumber(), sms.getSubscriptionId());
            try (SQLiteDatabase db = PAssist.getWritableDatabase()) {
              try (Cursor cursor = db.query(TABLE_TEST_ALERT_STATE, new String[]{TABLE_TEST_ALERT_STATE_COLUMN_ID}, TABLE_TEST_ALERT_STATE_COLUMN_ID + "=?", new String[]{id}, null, null, null)) {
                if (cursor.getCount() == 0) {
                  ContentValues contentValues = new ContentValues();
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_ID, id);
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_LAST_RECEIVED_TIME, Instant.now().toEpochMilli());
                  contentValues.put(TABLE_TEST_ALERT_STATE_COLUMN_MESSAGE, sms.getText());
                  db.insert(TABLE_TEST_ALERT_STATE, null, contentValues);
                  Set<String> superviseTestContexts = SharedState.getSuperviseTestContexts(context);
                  superviseTestContexts.add(id);
                  SharedState.setSuperviseTestContexts(context, superviseTestContexts);
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
            new AlarmService(context).newAlarm(sms);
          }
        }
      }
      context.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    }
  }

}
