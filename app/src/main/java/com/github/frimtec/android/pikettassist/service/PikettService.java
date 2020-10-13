package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.service.system.AlarmService;

import static com.github.frimtec.android.pikettassist.action.JobService.PIKETT_SERVICE;

public class PikettService extends ReScheduleJobIntentService {

  public PikettService() {
    super(PIKETT_SERVICE);
  }

  @Override
  protected ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService) {
    return new PikettServiceWorkUnit(new ShiftService(context), context);
  }

  public static void enqueueWork(Context context) {
    ReScheduleJobIntentService.enqueueWork(context, PIKETT_SERVICE, PikettService.class);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduleJobIntentService.enqueueWork(context, PIKETT_SERVICE, PikettService.class, intent);
  }
}
