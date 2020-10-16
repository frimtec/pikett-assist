package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

import static com.github.frimtec.android.pikettassist.action.JobService.PIKETT_SERVICE;

public class PikettService extends ReScheduleJobIntentService {

  public PikettService() {
    super(PIKETT_SERVICE);
  }

  @Override
  protected ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService) {
    return new PikettServiceWorkUnit(
        ApplicationState.instance(),
        ApplicationPreferences.instance(),
        new ShiftService(context),
        new NotificationService(context),
        new VolumeService(context),
        () -> {
          LowSignalService.enqueueWork(context);
          TestAlarmService.enqueueWork(context);
        },
        context);
  }

  public static void enqueueWork(Context context) {
    ReScheduleJobIntentService.enqueueWork(context, PIKETT_SERVICE, PikettService.class);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduleJobIntentService.enqueueWork(context, PIKETT_SERVICE, PikettService.class, intent);
  }
}
