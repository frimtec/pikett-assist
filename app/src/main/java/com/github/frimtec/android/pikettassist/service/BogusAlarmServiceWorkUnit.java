package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.BogusAlarmService.AlarmType;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertActivity;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;

import java.util.Optional;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmService.INTENT_EXTRA_ALARM_TYPE;

class BogusAlarmServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "BogusAlarmServiceWorkUnit";

  private final AlarmService alarmService;
  private final Context context;

  public BogusAlarmServiceWorkUnit(AlarmService alarmService, Context context) {
    this.alarmService = alarmService;
    this.context = context;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    AlarmType type = AlarmType.valueOf(intent.getStringExtra(INTENT_EXTRA_ALARM_TYPE));
    Log.i(TAG, "Trigger bogus alarm for " + type);
    switch (type) {
      case ALERT:
        AlertActivity.trigger(SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK, null, context);
        break;
      case LOW_SIGNAL:
        LowSignalAlarmActivity.trigger(context, alarmService, false);
        break;
      case MISSING_TEST_ALARM:
        MissingTestAlarmAlarmActivity.trigger(context, alarmService);
        break;
      default:
        Log.e(TAG, "Unknown type " + type);
    }
    return Optional.empty();
  }
}
