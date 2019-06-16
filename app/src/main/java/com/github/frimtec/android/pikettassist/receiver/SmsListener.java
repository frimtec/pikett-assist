package com.github.frimtec.android.pikettassist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.PikettService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.activity.MainActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.PikettState;
import com.github.frimtec.android.pikettassist.domain.Sms;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SmsHelper;

import java.time.Instant;

import static com.github.frimtec.android.pikettassist.helper.NotificationHelper.ACTION_CONFIRM;
import static com.github.frimtec.android.pikettassist.helper.SmsHelper.confimSms;

public class SmsListener extends BroadcastReceiver {

  private static final String TAG = "SmsListener";
  private SharedPreferences preferences;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
      if (SharedState.getPikettState(context) == PikettState.OFF) {
        Log.d(TAG, "SMS recived but not on pikett");
        return;
      }
      String pikettNumber = SharedState.getSmsSenderNumber(context);
      for (Sms sms : SmsHelper.getSmsFromIntent(intent)) {
        if (sms.getNumber().equals(pikettNumber)) {
          Log.d(TAG, "SMS from pikett number");
          if (sms.getText().matches(SharedState.getSmsTestMessagePattern(context))) {
            Log.d(TAG, "TEST alarm");
            SharedState.setSmsLastTestMessageReceivedTime(context, Instant.now());
            confimSms(pikettNumber);
          } else {
            Log.d(TAG, "Alarm");
            if(SharedState.getAlarmState(context) != AlarmState.ON) {
              SharedState.setAlarmState(context, AlarmState.ON);
              context.startService(new Intent(context, AlertService.class));
            }
          }
        }
      }
    }
  }
}
