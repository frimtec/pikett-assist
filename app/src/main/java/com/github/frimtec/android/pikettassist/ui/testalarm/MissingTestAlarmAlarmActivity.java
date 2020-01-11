package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

import java.util.Collections;

public class MissingTestAlarmAlarmActivity extends AbstractAlarmActivity {

  private static final String TAG = "MissingTestAlarmAlarmActivity";

  public MissingTestAlarmAlarmActivity() {
    super(TAG, R.string.notification_missing_test_alert_title, Pair.create(200, 1000), SwipeButtonStyle.BLUE);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRingtone(RingtoneManager.getRingtone(this,  getAlarmTone(this)));
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = SharedState.getTestAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
  }

  public static void trigger(Context context, AlarmService alarmService) {
    AbstractAlarmActivity.trigger(MissingTestAlarmAlarmActivity.class, context, alarmService, Collections.emptyList());
  }

}