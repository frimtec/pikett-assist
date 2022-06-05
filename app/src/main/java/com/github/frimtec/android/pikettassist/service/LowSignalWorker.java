package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.action.JobService.LOW_SIGNAL_SERVICE;
import static com.github.frimtec.android.pikettassist.service.LowSignalWorkUnit.EXTRA_FILTER_STATE;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.dao.AlertDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService;
import com.github.frimtec.android.pikettassist.service.system.VolumeService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;

public class LowSignalWorker extends ReScheduledWorker {

  public LowSignalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @Override
  protected JobService getJobService() {
    return LOW_SIGNAL_SERVICE;
  }

  @Override
  protected WorkUnit geServiceWorkUnit(Context context, AlarmService alarmService) {
    return new LowSignalWorkUnit(
        ApplicationPreferences.instance(),
        (AudioManager) context.getSystemService(Context.AUDIO_SERVICE),
        new AlertDao(),
        new ShiftService(context),
        new SignalStrengthService(context),
        new VolumeService(context),
        new NotificationService(context),
        () -> LowSignalAlarmActivity.trigger(context, alarmService, true),
        context
    );
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduledWorker.enqueueWork(
        context,
        LOW_SIGNAL_SERVICE,
        LowSignalWorker.class,
        new Data.Builder()
            .putInt(EXTRA_FILTER_STATE, intent.getIntExtra(EXTRA_FILTER_STATE, 0))
            .build()
    );
  }

  public static void enqueueWork(Context context) {
    ReScheduledWorker.enqueueWork(context, LOW_SIGNAL_SERVICE, LowSignalWorker.class);
  }
}
