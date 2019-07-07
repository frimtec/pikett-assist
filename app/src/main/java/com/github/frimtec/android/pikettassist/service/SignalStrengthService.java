package com.github.frimtec.android.pikettassist.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.*;
import android.util.Log;
import com.github.frimtec.android.pikettassist.helper.CalendarEventHelper;
import com.github.frimtec.android.pikettassist.helper.NotificationHelper;
import com.github.frimtec.android.pikettassist.helper.SignalStremgthHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.List;
import java.util.Optional;

import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_POOR;

public class SignalStrengthService extends Service {

  private static final String TAG = "SignalStrengthService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

    Optional<Integer> signalStrength = SignalStremgthHelper.getSignalStrength(this);
    if (isLowSignal(signalStrength)) {
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      // Start without a delay
      // Vibrate for 100 milliseconds
      // Sleep for 1000 milliseconds
      long[] pattern = {0, 100, 500};
      vibrator.vibrate(pattern, 0);
      NotificationHelper.notifySignalLow(this, signalStrength);
      do {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Log.e(TAG, "Unexpected interrupt", e);
        }
        signalStrength = SignalStremgthHelper.getSignalStrength(this);
      } while (isLowSignal(signalStrength));
      vibrator.cancel();
    }
    stopSelf();
    return START_NOT_STICKY;
  }


  private boolean isLowSignal(Optional<Integer> signalStrength) {
    return !signalStrength.isPresent() || signalStrength.get() <= SIGNAL_STRENGTH_POOR;
    }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (CalendarEventHelper.hasPikettEventForNow(this, SharedState.getCalendarEventPikettTitlePattern(this))) {
      AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
      alarm.setExactAndAllowWhileIdle(alarm.RTC_WAKEUP, System.currentTimeMillis() + CHECK_INTERVAL_MS,
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    }
  }
}
