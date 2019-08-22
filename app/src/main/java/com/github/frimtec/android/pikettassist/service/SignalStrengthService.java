package com.github.frimtec.android.pikettassist.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper.SignalLevel;
import com.github.frimtec.android.pikettassist.helper.VibrateHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.List;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;

public class SignalStrengthService extends IntentService {

  private static final String TAG = "SignalStrengthService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;
  public SignalStrengthService() {
    super(TAG);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.d(TAG, "Service cycle");

    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

    SignalLevel iniTialLevel = SignalStremgthHelper.getSignalStrength(this);
    if (SharedState.getSuperviseSignalStrength(this) && isCallStateIdle() && isLowSignal(iniTialLevel)) {
      this.sendBroadcast(new Intent("com.github.frimtec.android.pikettassist.refresh"));
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 100, 500}, 1));
      VibrateHelper.vibrateWhileDoing(getApplicationContext(), 100, 500, () -> {
        SignalLevel level = iniTialLevel;
        NotificationHelper.notifySignalLow(this, level);
        do {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            Log.e(TAG, "Unexpected interrupt", e);
          }
          level = SignalStremgthHelper.getSignalStrength(this);
        } while (isLowSignal(level));

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
      alarm.setExactAndAllowWhileIdle(alarm.RTC_WAKEUP, System.currentTimeMillis() + CHECK_INTERVAL_MS,
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    } else {
      Log.d(TAG, "SignalStrengthService stopped as pikett state is OFF");
    }
  }
}
