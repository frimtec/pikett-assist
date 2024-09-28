package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;

import java.util.Optional;

abstract class ReScheduledWorker extends Worker {

  private static final String TAG = "ReScheduledWorker";

  public ReScheduledWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  protected abstract JobService getJobService();

  protected abstract WorkUnit geServiceWorkUnit(Context context);

  @NonNull
  @Override
  public Result doWork() {
    Data inputData = getInputData();
    Context context = getApplicationContext();
    AlarmService alarmService = new AlarmService(context);
    Optional<AlarmService.ScheduleInfo> scheduleInfo = geServiceWorkUnit(context).apply(inputData);
    JobService jobService = getJobService();
    if (scheduleInfo.isPresent()) {
      alarmService.setAlarmForJob(scheduleInfo.get(), jobService);
      Log.i(TAG, String.format("[%s] Scheduled for next work cycle in %s", jobService, scheduleInfo.get().getScheduleDelay()));
    } else {
      Log.i(TAG, String.format("[%s] Stopped", jobService));
    }
    return Result.success();
  }

  protected static void enqueueWork(
      Context context,
      JobService jobService,
      Class<? extends ReScheduledWorker> workerClass
  ) {
    enqueueWork(context, jobService, workerClass, new Data.Builder().build());
  }

  protected static void enqueueWork(
      Context context,
      JobService jobService,
      Class<? extends ReScheduledWorker> workerClass,
      Data inputData
  ) {
    WorkManager workManager = WorkManager.getInstance(context);
    workManager.enqueueUniqueWork(
        jobService.name(),
        ExistingWorkPolicy.REPLACE,
        new OneTimeWorkRequest.Builder(workerClass)
            .setInputData(inputData)
            .build()
    );
  }
}
