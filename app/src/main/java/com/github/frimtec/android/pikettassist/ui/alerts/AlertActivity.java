package com.github.frimtec.android.pikettassist.ui.alerts;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

import java.util.Arrays;

public class AlertActivity extends AbstractAlarmActivity {

  private static final String EXTRA_SMS_NUMBER = "sms_number";
  private static final String EXTRA_SUBSCRIPTION_ID = "subscriptionId";

  private static final String TAG = "AlertActivity";

  private AlertService alertService;

  public AlertActivity() {
    super(TAG, R.string.notification_alert_title, Pair.create(400, 200), SwipeButtonStyle.ALARM);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alertService = new AlertService(this);
    String smsNumber = getIntent().getStringExtra(EXTRA_SMS_NUMBER);
    String subscriptionId = getIntent().getStringExtra(EXTRA_SUBSCRIPTION_ID);
    setSwipeAction(() -> alertService.confirmAlert(this, smsNumber, subscriptionId != null ? Integer.valueOf(subscriptionId) : null));
    setRingtone(RingtoneManager.getRingtone(this, getAlarmTone(this)));
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = ApplicationPreferences.instance().getAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  public static void trigger(String smsNumber, Integer subscriptionId, Context context) {
    AbstractAlarmActivity.trigger(
        AlertActivity.class,
        context,
        new AlarmService(context),
        Arrays.asList(Pair.create(EXTRA_SMS_NUMBER, smsNumber), Pair.create(EXTRA_SUBSCRIPTION_ID, subscriptionId != null ? String.valueOf(subscriptionId) : null))
    );
  }

}