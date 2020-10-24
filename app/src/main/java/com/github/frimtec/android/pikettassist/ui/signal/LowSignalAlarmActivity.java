package com.github.frimtec.android.pikettassist.ui.signal;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.isLowSignal;

public class LowSignalAlarmActivity extends AbstractAlarmActivity {

  private static final String TAG = "LowSignalAlarmActivity";

  private static final String EXTRA_AUTO_CLOSE = "auto_close";

  private final AtomicBoolean autoClose = new AtomicBoolean(false);

  public LowSignalAlarmActivity() {
    super(TAG, R.string.notification_low_signal_title, Pair.create(100, 500), SwipeButtonStyle.NO_SIGNAL);
    setEndCondition(() -> {
      if(!autoClose.get()) {
        return false;
      }
      SignalStrengthService.SignalLevel level = new SignalStrengthService(LowSignalAlarmActivity.this).getSignalStrength();
      return !isLowSignal(level, ApplicationPreferences.instance().getSuperviseSignalStrengthMinLevel(this)) || !ApplicationPreferences.instance().getSuperviseSignalStrength(getApplicationContext());
    }, Duration.ofSeconds(1));
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.autoClose.set(Boolean.parseBoolean(getIntent().getStringExtra(EXTRA_AUTO_CLOSE)));
  }

  public static void trigger(Context context, AlarmService alarmService, boolean autoClose) {
    AbstractAlarmActivity.trigger(
        LowSignalAlarmActivity.class,
        context,
        alarmService,
        Collections.singletonList(Pair.create(EXTRA_AUTO_CLOSE, Boolean.toString(autoClose)))
    );
  }

}