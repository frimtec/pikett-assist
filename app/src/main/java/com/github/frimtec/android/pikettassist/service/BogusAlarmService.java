package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.action.JobService.BOGUS_ALARM_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertActivity;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;
import com.github.frimtec.android.securesmsproxyapi.SecureSmsProxyFacade;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

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
    Map<AlarmType, Runnable> alarmTriggers = new EnumMap<>(AlarmType.class);
    alarmTriggers.put(AlarmType.ALERT, () -> AlertActivity.trigger(SecureSmsProxyFacade.PHONE_NUMBER_LOOPBACK, null, context));
    alarmTriggers.put(AlarmType.LOW_SIGNAL, () -> LowSignalAlarmActivity.trigger(context, alarmService, false));
    alarmTriggers.put(AlarmType.MISSING_TEST_ALARM, () -> MissingTestAlarmAlarmActivity.trigger(context, alarmService));
    return new BogusAlarmServiceWorkUnit(alarmTriggers);
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
