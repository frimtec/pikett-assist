package com.github.frimtec.android.pikettassist.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.state.DbFactory;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;
import com.github.frimtec.android.pikettassist.domain.AlarmState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.utility.NotificationHelper;
import com.github.frimtec.android.pikettassist.utility.SignalStrengthHelper;
import com.github.frimtec.android.pikettassist.utility.SignalStrengthHelper.SignalLevel;
import com.github.frimtec.android.pikettassist.utility.VolumeHelper;
import com.github.frimtec.android.pikettassist.state.SharedState;

import org.threeten.bp.LocalTime;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;

public class SignalStrengthService extends IntentService {

  private static final String TAG = "SignalStrengthService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;

  private AlarmManager alarmManager;
  private TelephonyManager telephonyManager;
  private boolean pikettState = false;

  public SignalStrengthService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
    this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    this.pikettState = SharedState.getPikettState(this) == OnOffState.ON;
    SignalLevel level = new SignalStrengthHelper(this).getSignalStrength();
    if (this.pikettState && SharedState.getSuperviseSignalStrength(this) && isCallStateIdle() && !isAlarmStateOn() && isLowSignal(this, level)) {
      NotificationHelper.notifySignalLow(this, level);
      LowSignalAlarmActivity.trigger(this, this.alarmManager);
    }
  }

  private boolean isAlarmStateOn() {
    return SharedState.getAlarmState(DbFactory.instance()).first == AlarmState.ON;
  }

  private boolean isCallStateIdle() {
    return this.telephonyManager.getCallState() == CALL_STATE_IDLE;
  }

  public static boolean isLowSignal(Context context, SignalLevel level) {
    return level.ordinal() <= SharedState.getSuperviseSignalStrengthMinLevel(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (this.pikettState) {
      if (SharedState.getManageVolumeEnabled(this)) {
        new VolumeHelper(this).setVolume(SharedState.getOnCallVolume(this, LocalTime.now()));
      }
      this.alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + CHECK_INTERVAL_MS,
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    } else {
      Log.i(TAG, "SignalStrengthService stopped as pikett state is OFF");
    }
  }
}
