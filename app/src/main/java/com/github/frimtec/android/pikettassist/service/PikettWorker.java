package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.action.JobService.PIKETT_SERVICE;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.state.ApplicationState;

public class PikettWorker extends ReScheduledWorker {

  public PikettWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @Override
  protected JobService getJobService() {
    return PIKETT_SERVICE;
  }

  @Override
  protected WorkUnit geServiceWorkUnit(Context context) {
    return new PikettWorkUnit(
        ApplicationState.instance(),
        ApplicationPreferences.instance(),
        new ShiftService(context),
        new NotificationService(context),
        new VolumeService(context),
        () -> {
          LowSignalWorker.enqueueWork(context);
          TestAlarmWorker.enqueueWork(context);
        },
        context
    );
  }

  public static void enqueueWork(Context context, @SuppressWarnings("unused") Intent intent) {
    ReScheduledWorker.enqueueWork(
        context,
        PIKETT_SERVICE,
        PikettWorker.class,
        new Data.Builder()
            .build()
    );
  }

  public static void enqueueWork(Context context) {
    ReScheduledWorker.enqueueWork(context, PIKETT_SERVICE, PikettWorker.class);
  }
}
