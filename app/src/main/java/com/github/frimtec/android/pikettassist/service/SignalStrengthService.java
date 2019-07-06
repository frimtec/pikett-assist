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
import com.github.frimtec.android.pikettassist.state.SharedState;

import java.util.List;

import static android.telephony.CellSignalStrength.SIGNAL_STRENGTH_POOR;

public class SignalStrengthService extends Service {

  private static final String TAG = "SignalStrengthService";

  private boolean lowSignal = false;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    @SuppressLint("MissingPermission") List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();

    CellSignalStrength signalStrength = null;
    if (cellInfos.size() == 0) {
      Log.w(TAG, "No signal");
    } else {
      CellInfo cellInfo = cellInfos.get(0);
      if (cellInfo instanceof CellInfoGsm) {
        signalStrength = ((CellInfoGsm) cellInfo).getCellSignalStrength();
      } else if (cellInfo instanceof CellInfoLte) {
        signalStrength = ((CellInfoLte) cellInfo).getCellSignalStrength();
      } else {
        Log.e(TAG, "Unknown cell info type: " + cellInfos.getClass().getName());
      }
      if (signalStrength != null) {
        Log.v(TAG, String.format("Signal strength dbm: %d; level: %d; asu: %d", signalStrength.getDbm(), signalStrength.getLevel(), signalStrength.getAsuLevel()));
      }
    }

    if(signalStrength == null || signalStrength.getLevel() <= SIGNAL_STRENGTH_POOR) {
      lowSignal = true;
      Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
      vibrator.vibrate(VibrationEffect.createOneShot(900, VibrationEffect.DEFAULT_AMPLITUDE));
      NotificationHelper.notifySignalLow(this, signalStrength);
    } else {
      lowSignal = false;
    }
    stopSelf();
    return START_NOT_STICKY;
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
      alarm.set(alarm.RTC_WAKEUP, System.currentTimeMillis() + (lowSignal ? 1000 : 60 * 1000),
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    }
  }
}
