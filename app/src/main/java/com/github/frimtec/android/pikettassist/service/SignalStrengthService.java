package com.github.frimtec.android.pikettassist.service;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.state.SharedState;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;

import org.threeten.bp.LocalTime;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;

public class SignalStrengthService extends IntentService {

  private static final String TAG = "SignalStrengthService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;

  private AlarmManager alarmManager;
  private TelephonyManager telephonyManager;
  private boolean pikettState = false;
  private AlertDao alertDao;
  private ShiftService shiftService;

  public SignalStrengthService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
    this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    this.alertDao = new AlertDao();
    this.shiftService = new ShiftService(this);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    this.pikettState = this.shiftService.getState() == OnOffState.ON;
    SignalLevel level = new com.github.frimtec.android.pikettassist.service.system.SignalStrengthService(this).getSignalStrength();
    if (this.pikettState && SharedState.getSuperviseSignalStrength(this) && isCallStateIdle() && !isAlarmStateOn() && isLowSignal(this, level)) {
      new NotificationService(this).notifySignalLow(level);
      LowSignalAlarmActivity.trigger(this, this.alarmManager);
    }
  }

  private boolean isAlarmStateOn() {
    return this.alertDao.getAlertState().first == AlertState.ON;
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
        new VolumeService(this).setVolume(SharedState.getOnCallVolume(this, LocalTime.now()));
      }
      this.alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + CHECK_INTERVAL_MS,
          PendingIntent.getService(this, 0, new Intent(this, SignalStrengthService.class), 0)
      );
    } else {
      Log.i(TAG, "SignalStrengthService stopped as pikett state is OFF");
    }
  }
}
