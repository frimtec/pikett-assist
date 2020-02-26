package com.github.frimtec.android.pikettassist.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.frimtec.android.pikettassist.domain.AlertState;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;

import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static com.github.frimtec.android.pikettassist.state.ApplicationPreferences.PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR;

public class LowSignalService extends IntentService {

  private static final String TAG = "LowSignalService";
  private static final int CHECK_INTERVAL_MS = 60 * 1000;
  public static final String EXTRA_FILTER_STATE = "FILTER_STATE";

  private AlarmService alarmService;
  private TelephonyManager telephonyManager;
  private boolean pikettState = false;
  private AlertDao alertDao;
  private ShiftService shiftService;

  private int currentFilterState = 0;

  public LowSignalService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    this.alertDao = new AlertDao();
    this.shiftService = new ShiftService(this);
    this.alarmService = new AlarmService(this);
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(TAG, "Service cycle");
    this.currentFilterState = intent.getIntExtra(EXTRA_FILTER_STATE, 0);
    this.pikettState = this.shiftService.getState() == OnOffState.ON;
    SignalStrengthService signalStrengthService = new SignalStrengthService(this);
    SignalLevel level = signalStrengthService.getSignalStrength();
    if (this.pikettState && ApplicationPreferences.getSuperviseSignalStrength(this) && isCallStateIdle() && !isAlarmStateOn() && isLowSignal(this, level)) {
      int lowSignalFilter = ApplicationPreferences.getLowSignalFilter(this);
      if (lowSignalFilter > 0 && level != SignalLevel.OFF) {
        if (this.currentFilterState < lowSignalFilter) {
          Log.d(TAG, "Filter round: " + this.currentFilterState);
          this.currentFilterState += 1;
          return;
        } else {
          this.currentFilterState = lowSignalFilter + 1;
          Log.d(TAG, "Filter triggered, alarm raced");
        }
      }
      if (ApplicationPreferences.getNotifyLowSignal(this)) {
        new NotificationService(this).notifySignalLow(level);
      }
      LowSignalAlarmActivity.trigger(this, this.alarmService);
    } else {
      if (this.currentFilterState > 0) {
        Log.d(TAG, "Filter stopped, signal ok");
        this.currentFilterState = 0;
      }
    }
  }

  private boolean isAlarmStateOn() {
    return this.alertDao.getAlertState().first == AlertState.ON;
  }

  private boolean isCallStateIdle() {
    return this.telephonyManager.getCallState() == CALL_STATE_IDLE;
  }

  public static boolean isLowSignal(Context context, SignalLevel level) {
    return level.ordinal() <= ApplicationPreferences.getSuperviseSignalStrengthMinLevel(context);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (this.pikettState) {
      if (ApplicationPreferences.getManageVolumeEnabled(this)) {
        new VolumeService(this).setVolume(ApplicationPreferences.getOnCallVolume(this, LocalTime.now()));
      }
      Intent intent = new Intent(this, LowSignalService.class);
      long nextRunInMillis = CHECK_INTERVAL_MS;
      if (this.currentFilterState > 0) {
        intent.putExtra(EXTRA_FILTER_STATE, this.currentFilterState);
        if (this.currentFilterState <= ApplicationPreferences.getLowSignalFilter(this)) {
          nextRunInMillis = Duration.ofSeconds(PREF_KEY_LOW_SIGNAL_FILTER_TO_SECONDS_FACTOR).toMillis();
        }
      }
      this.alarmService.setAlarmRelative(nextRunInMillis, intent);
    } else {
      Log.i(TAG, "LowSignalService stopped as pikett state is OFF");
    }
  }
}
