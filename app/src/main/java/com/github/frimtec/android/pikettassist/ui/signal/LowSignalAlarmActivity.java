package com.github.frimtec.android.pikettassist.ui.signal;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.InternetAvailabilityService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.common.AbstractAlarmActivity;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class LowSignalAlarmActivity extends AbstractAlarmActivity {

  private static final String TAG = "LowSignalAlarmActivity";

  private static final String EXTRA_AUTO_CLOSE = "auto_close";

  private final AtomicBoolean autoClose = new AtomicBoolean(false);

  public LowSignalAlarmActivity() {
    super(
        TAG,
        getTitleSupplier(),
        Pair.create(100, 500),
        SwipeButtonStyle.NO_SIGNAL
    );
    setEndCondition(() -> {
      if (!autoClose.get()) {
        return false;
      }
      ApplicationPreferences applicationPreferences = ApplicationPreferences.instance();
      Context context = getApplicationContext();

      if (!applicationPreferences.getSuperviseSignalStrength(context)) {
        return true;
      }
      if (isLowSignal(context, applicationPreferences)) {
        return false;
      }
      return !(applicationPreferences.getAlertConfirmMethod(context).isInternet() && isNoInternet(applicationPreferences));
    }, Duration.ofSeconds(1));
  }

  private static Function<Context, Integer> getTitleSupplier() {
    return (context) -> isLowSignal(context, ApplicationPreferences.instance()) ?
        R.string.notification_low_signal_title : R.string.notification_no_internet_title;
  }

  private static boolean isLowSignal(
      Context context,
      ApplicationPreferences applicationPreferences
  ) {
    return SignalStrengthService.isLowSignal(
        new SignalStrengthService(context).getSignalStrength(),
        applicationPreferences.getSuperviseSignalStrengthMinLevel(context)
    );
  }

  private boolean isNoInternet(ApplicationPreferences applicationPreferences) {
    Context context = getApplicationContext();
    return !(applicationPreferences.getAlertConfirmMethod(context).isInternet() &&
        new InternetAvailabilityService(context).getInternetAvailability().isAvailable());
  }

  @Override
  protected void doOnCreate(@Nullable Bundle savedInstanceState) {
    super.doOnCreate(savedInstanceState);
    this.autoClose.set(Boolean.parseBoolean(getIntent().getStringExtra(EXTRA_AUTO_CLOSE)));
  }

  public static void trigger(Context context, boolean autoClose) {
    AbstractAlarmActivity.trigger(
        LowSignalAlarmActivity.class,
        context,
        Collections.singletonList(Pair.create(EXTRA_AUTO_CLOSE, Boolean.toString(autoClose)))
    );
  }

}