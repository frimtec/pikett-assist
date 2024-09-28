package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.action.JobService.TEST_ALARM_SERVICE;
import static com.github.frimtec.android.pikettassist.service.TestAlarmWorkUnit.PARAM_INITIAL;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;

public class TestAlarmWorker extends ReScheduledWorker {

  public TestAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @Override
  protected JobService getJobService() {
    return TEST_ALARM_SERVICE;
  }

  @Override
  protected WorkUnit geServiceWorkUnit(Context context) {
    return new TestAlarmWorkUnit(
        ApplicationPreferences.instance(),
        new TestAlarmDao(),
        new ShiftService(context),
        new NotificationService(context),
        () -> MissingTestAlarmAlarmActivity.trigger(context),
        context
    );
  }

  public static void enqueueWork(Context context, @SuppressWarnings("unused") Intent intent) {
    ReScheduledWorker.enqueueWork(
        context,
        TEST_ALARM_SERVICE,
        TestAlarmWorker.class,
        new Data.Builder()
            .putBoolean(PARAM_INITIAL, intent.getBooleanExtra(PARAM_INITIAL, true))
            .build()
    );
  }

  public static void enqueueWork(Context context) {
    ReScheduledWorker.enqueueWork(context, TEST_ALARM_SERVICE, TestAlarmWorker.class);
  }
}
