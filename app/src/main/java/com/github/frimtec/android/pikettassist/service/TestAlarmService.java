package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;

import com.github.frimtec.android.pikettassist.service.dao.TestAlarmDao;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.NotificationService;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;

import static com.github.frimtec.android.pikettassist.action.JobService.TEST_ALARM_SERVICE;

public class TestAlarmService extends ReScheduleJobIntentService {

  public TestAlarmService() {
    super(TEST_ALARM_SERVICE);
  }

  @Override
  protected ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService) {
    return new TestAlarmServiceWorkUnit(
        ApplicationPreferences.instance(),
        new TestAlarmDao(),
        new ShiftService(context),
        new NotificationService(context),
        () -> MissingTestAlarmAlarmActivity.trigger(context, alarmService),
        context
    );
  }

  public static void enqueueWork(Context context) {
    ReScheduleJobIntentService.enqueueWork(context, TEST_ALARM_SERVICE, TestAlarmService.class);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduleJobIntentService.enqueueWork(context, TEST_ALARM_SERVICE, TestAlarmService.class, intent);
  }
}
