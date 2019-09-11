package com.github.frimtec.android.pikettassist.activity;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Pair;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.time.Duration;
import java.util.Collections;

import static com.github.frimtec.android.pikettassist.service.SignalStrengthService.isLowSignal;

public class LowSignalAlarmActivity extends AbstractAlarmActivity {

  private static final String TAG = "LowSignalAlarmActivity";

  public LowSignalAlarmActivity() {
    super(TAG, R.string.notification_low_signal_title, Pair.create(100, 500), SwipeButtonStyle.GREEN);
    setEndCondition(() -> {
      SignalStrengthHelper.SignalLevel level = SignalStrengthHelper.getSignalStrength(LowSignalAlarmActivity.this);
      return !isLowSignal(level) || !SharedState.getSuperviseSignalStrength(getApplicationContext());
    }, Duration.ofSeconds(1));
  }

  public static void trigger(Context context, AlarmManager alarmManager) {
    AbstractAlarmActivity.trigger(LowSignalAlarmActivity.class, context, alarmManager, Collections.emptyList());
  }

}