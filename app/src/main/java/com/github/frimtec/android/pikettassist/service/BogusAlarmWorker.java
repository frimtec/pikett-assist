package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.action.JobService.BOGUS_ALARM_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.action.JobService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;
import com.github.frimtec.android.pikettassist.ui.alerts.AlertActivity;
import com.github.frimtec.android.pikettassist.ui.signal.LowSignalAlarmActivity;
import com.github.frimtec.android.pikettassist.ui.testalarm.MissingTestAlarmAlarmActivity;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public class BogusAlarmWorker extends ReScheduledWorker {

  public static final String ALARM_TYPE_PARAMETER_KEY = "alarm_type";

  public enum AlarmType {
    ALERT,
    LOW_SIGNAL,
    MISSING_TEST_ALARM
  }

  public BogusAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @Override
  protected JobService getJobService() {
    return BOGUS_ALARM_SERVICE;
  }

  @Override
  protected WorkUnit geServiceWorkUnit(Context context) {
    Map<AlarmType, Runnable> alarmTriggers = new EnumMap<>(AlarmType.class);
    alarmTriggers.put(AlarmType.ALERT, () -> AlertActivity.trigger(context));
    alarmTriggers.put(AlarmType.LOW_SIGNAL, () -> LowSignalAlarmActivity.trigger(context, false));
    alarmTriggers.put(AlarmType.MISSING_TEST_ALARM, () -> MissingTestAlarmAlarmActivity.trigger(context));
    return new BogusAlarmWorkUnit(alarmTriggers);
  }

  public static void enqueueWork(Context context, Intent intent) {
    ReScheduledWorker.enqueueWork(
        context,
        BOGUS_ALARM_SERVICE,
        BogusAlarmWorker.class,
        new Data.Builder()
            .putString(ALARM_TYPE_PARAMETER_KEY, intent.getStringExtra(ALARM_TYPE_PARAMETER_KEY))
            .build()
    );
  }

  public static void scheduleBogusAlarm(Context context, AlarmType alarmType) {
    new AlarmService(context).setAlarmForJob(
        new ScheduleInfo(
            Duration.ofSeconds(10),
            intent -> intent.putExtra(ALARM_TYPE_PARAMETER_KEY, alarmType.name())
        ),
        BOGUS_ALARM_SERVICE
    );
    Toast.makeText(context, R.string.toast_bogus_alarm, Toast.LENGTH_SHORT)
        .show();
  }

}
