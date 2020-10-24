package com.github.frimtec.android.pikettassist.service;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import org.threeten.bp.Duration;

import static com.github.frimtec.android.pikettassist.action.JobService.BOGUS_ALARM_SERVICE;

public class BogusAlarmService extends ReScheduleJobIntentService {

  public static final String INTENT_EXTRA_ALARM_TYPE = "alarm_type";

  public enum AlarmType {
    ALERT,
    LOW_SIGNAL,
    MISSING_TEST_ALARM
  }

  public BogusAlarmService() {
    super(BOGUS_ALARM_SERVICE);
  }

  @Override
  protected ServiceWorkUnit createWorkUnit(Context context, AlarmService alarmService) {
    return new BogusAlarmServiceWorkUnit(alarmService, context);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduleJobIntentService.enqueueWork(context, BOGUS_ALARM_SERVICE, BogusAlarmService.class, intent);
  }

  public static void scheduleBogusAlarm(Context context, AlarmType alarmType) {
    new AlarmService(context).setAlarmForJob(new ScheduleInfo(
        Duration.ofSeconds(10),
        intent -> intent.putExtra(INTENT_EXTRA_ALARM_TYPE, alarmType.name())
    ), BOGUS_ALARM_SERVICE);
    Toast.makeText(context, R.string.toast_bogus_alarm, Toast.LENGTH_SHORT).show();
  }

}
