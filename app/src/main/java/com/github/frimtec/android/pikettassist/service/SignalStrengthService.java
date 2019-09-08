package com.github.frimtec.android.pikettassist.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStrengthHelper.SignalLevel;
import com.github.frimtec.android.pikettassist.helper.VibrateHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;

public class SignalStrengthService extends IntentService {

  private static final String TAG = "SignalStrengthService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;

  public SignalStrengthService() {
    super(TAG);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    SignalLevel initialSignalLevel = SignalStrengthHelper.getSignalStrength(this);
    if (SharedState.getSuperviseSignalStrength(this) && isCallStateIdle() && isLowSignal(initialSignalLevel)) {
      this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      VibrateHelper.vibrateWhileDoing(getApplicationContext(), 100, 500, () -> {
        SignalLevel level = initialSignalLevel;
        NotificationHelper.notifySignalLow(this, level);
        do {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Log.e(TAG, "Unexpected interrupt", e);
          }
          level = SignalStrengthHelper.getSignalStrength(this);
        } while (isLowSignal(level) && SharedState.getSuperviseSignalStrength(getApplicationContext()));

      });
      this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
    }
  }

  private boolean isCallStateIdle() {
    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    return telephonyManager.getCallState() == CALL_STATE_IDLE;
  }

  private boolean isLowSignal(SignalLevel level) {
    return level.ordinal() <= SignalLevel.POOR.ordinal();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (SharedState.getPikettState(this) == OnOffState.ON) {
      AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
      alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + CHECK_INTERVAL_MS,
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    } else {
      Log.i(TAG, "SignalStrengthService stopped as pikett state is OFF");
    }
  }
}
