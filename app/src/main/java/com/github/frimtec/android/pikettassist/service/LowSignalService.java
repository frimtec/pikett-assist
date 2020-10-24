package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;

import static com.github.frimtec.android.pikettassist.action.JobService.LOW_SIGNAL_SERVICE;

public class LowSignalService extends ReScheduleJobIntentService {

  public LowSignalService() {
    super(LOW_SIGNAL_SERVICE);
  }

  @Override
  protected ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService) {
    return new LowSignalServiceWorkUnit(
        ApplicationPreferences.instance(),
        (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE),
        new AlertDao(),
        new ShiftService(context),
        new SignalStrengthService(context),
        new VolumeService(context),
        new NotificationService(context),
        () -> LowSignalAlarmActivity.trigger(context, alarmService, true),
        context
    );
  }

  public static void enqueueWork(Context context) {
    ReScheduleJobIntentService.enqueueWork(context, LOW_SIGNAL_SERVICE, LowSignalService.class);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduleJobIntentService.enqueueWork(context, LOW_SIGNAL_SERVICE, LowSignalService.class, intent);
  }
}
