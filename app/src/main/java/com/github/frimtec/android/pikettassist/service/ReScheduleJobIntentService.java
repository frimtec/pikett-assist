package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import java.util.Optional;

abstract class ReScheduleJobIntentService extends JobIntentService {

  private static final String TAG = "ReScheduleJobIntentService";

  private final JobService jobService;
  private ServiceWorkUnit workUnit;
  private AlarmService alarmService;

  public ReScheduleJobIntentService(JobService jobService) {
    super();
    this.jobService = jobService;
  }

  @Override
  public final void onCreate() {
    super.onCreate();
    this.alarmService = new AlarmService(this);
    this.workUnit = createWorkUnit(this, alarmService);
  }

  protected abstract ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService);

  @Override
  protected final void onHandleWork(@NonNull Intent intent) {
    Log.i(TAG, String.format("[%s] Start work cycle", this.jobService));
    Optional<ScheduleInfo> scheduleInfo = this.workUnit.apply(intent);
    if (scheduleInfo.isPresent()) {
      this.alarmService.setAlarmForJob(scheduleInfo.get(), this.jobService);
      Log.d(TAG, String.format("[%s] Scheduled for next work cycle in %s", this.jobService, scheduleInfo.get().getScheduleDelay()));
    } else {
      Log.i(TAG, String.format("[%s] Stopped", this.jobService));
    }
  }

  protected static <T extends ReScheduleJobIntentService> void enqueueWork(Context context, JobService jobService, Class<T> serviceClass) {
    enqueueWork(context, serviceClass, jobService.getId(), new Intent(context, serviceClass));
  }

  protected static <T extends ReScheduleJobIntentService> void enqueueWork(Context context, JobService jobService, Class<T> serviceClass, Intent intent) {
    intent.setClass(context, serviceClass);
    enqueueWork(context, serviceClass, jobService.getId(), intent);
  }

}
