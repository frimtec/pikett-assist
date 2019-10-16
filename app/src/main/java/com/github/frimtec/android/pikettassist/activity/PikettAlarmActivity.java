package com.github.frimtec.android.pikettassist.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.AlarmService;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.Arrays;

public class PikettAlarmActivity extends AbstractAlarmActivity {

  private static final String EXTRA_SMS_NUMBER = "sms_number";
  private static final String EXTRA_SUBSCRIPTION_ID = "subscriptionId";

  private static final String TAG = "PikettAlarmActivity";

  private AlarmService alarmService;

  public PikettAlarmActivity() {
    super(TAG, R.string.notification_alert_title, Pair.create(400, 200), SwipeButtonStyle.RED);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alarmService = new AlarmService(this);
    String smsNumber = getIntent().getStringExtra(EXTRA_SMS_NUMBER);
    String subscriptionId = getIntent().getStringExtra(EXTRA_SUBSCRIPTION_ID);
    setSwipeAction(() -> alarmService.confirmAlarm(this, smsNumber, subscriptionId != null ? Integer.valueOf(subscriptionId) : null));
    setRingtone(RingtoneManager.getRingtone(this, getAlarmTone(this)));
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = SharedState.getAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  public static void trigger(String smsNumber, Integer subscriptionId, Context context) {
    AbstractAlarmActivity.trigger(
        PikettAlarmActivity.class,
        context,
        (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE),
        Arrays.asList(Pair.create(EXTRA_SMS_NUMBER, smsNumber), Pair.create(EXTRA_SUBSCRIPTION_ID, subscriptionId != null ? String.valueOf(subscriptionId) : null))
    );
  }

}