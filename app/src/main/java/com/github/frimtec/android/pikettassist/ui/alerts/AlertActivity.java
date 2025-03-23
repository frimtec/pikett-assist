package com.github.frimtec.android.pikettassist.ui.alerts;

import static java.util.Collections.emptyList;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.AlertService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

public class AlertActivity extends AbstractAlarmActivity {

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
  public void doOnCreate(Bundle savedInstanceState) {
    super.doOnCreate(savedInstanceState);
    this.alertService = new AlertService(this);
    setSwipeAction(() -> alertService.confirmAlert());
    setRingtone(RingtoneManager.getRingtone(this, getAlarmTone(this)));
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = ApplicationPreferences.instance().getAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
  }

  public static void trigger(Context context) {
    AbstractAlarmActivity.trigger(
        AlertActivity.class,
        context,
        emptyList()
    );
  }

}