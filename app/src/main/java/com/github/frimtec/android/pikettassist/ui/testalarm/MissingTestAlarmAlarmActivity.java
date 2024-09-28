package com.github.frimtec.android.pikettassist.ui.testalarm;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

import java.util.Collections;

public class MissingTestAlarmAlarmActivity extends AbstractAlarmActivity {

  private static final String TAG = "MissingTestAlarmAlarmActivity";

  public MissingTestAlarmAlarmActivity() {
    super(
        TAG,
        (context) -> R.string.notification_missing_test_alert_title,
        Pair.create(200, 1000),
        SwipeButtonStyle.MISSING_TEST_ALARM
    );
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRingtone(RingtoneManager.getRingtone(this,  getAlarmTone(this)));
  }

  private Uri getAlarmTone(Context context) {
    String alarmRingTone = ApplicationPreferences.instance().getTestAlarmRingTone(context);
    if (!alarmRingTone.isEmpty()) {
      return Uri.parse(alarmRingTone);
    }
    return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
  }

  public static void trigger(Context context) {
    AbstractAlarmActivity.trigger(MissingTestAlarmAlarmActivity.class, context, Collections.emptyList());
  }

}