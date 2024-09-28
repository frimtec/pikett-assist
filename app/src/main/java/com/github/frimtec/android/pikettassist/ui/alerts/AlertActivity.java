package com.github.frimtec.android.pikettassist.ui.alerts;

import static java.util.Collections.singletonList;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;
import com.github.frimtec.android.pikettassist.util.GsonHelper;
import com.github.frimtec.android.securesmsproxyapi.Sms;
import com.google.gson.JsonSyntaxException;

public class AlertActivity extends AbstractAlarmActivity {

  private static final String EXTRA_SMS = "sms";

  private static final String TAG = "AlertActivity";

  private AlertService alertService;

  public AlertActivity() {
    super(
        TAG,
        (context) -> R.string.notification_alert_title,
        Pair.create(400, 200),
        SwipeButtonStyle.ALARM
    );
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.alertService = new AlertService(this);
    setSwipeAction(() -> alertService.confirmAlert(this, extractSms(getIntent())));
    setRingtone(RingtoneManager.getRingtone(this, getAlarmTone(this)));
  }

  private static Sms extractSms(Intent intent) {
    String smsValue = intent.getStringExtra(EXTRA_SMS);
    try {
      return GsonHelper.GSON.fromJson(smsValue, Sms.class);
    } catch (JsonSyntaxException e) {
      Log.e(TAG, "Cannot parse last alarm sms: '" + smsValue + "'", e);
    }
    return null;
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = ApplicationPreferences.instance().getAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  public static void trigger(Sms sms, Context context) {
    AbstractAlarmActivity.trigger(
        AlertActivity.class,
        context,
        singletonList(Pair.create(EXTRA_SMS, GsonHelper.GSON.toJson(sms)))
    );
  }

}