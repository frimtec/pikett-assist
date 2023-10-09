package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.ALARM_TYPE_PARAMETER_KEY;

import android.util.Log;

import androidx.work.Data;

import com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.AlarmType;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class BogusAlarmWorkUnit implements WorkUnit {

  private static final String TAG = "BogusAlarmServiceWorkUnit";

  private final Map<AlarmType, Runnable> alarmTriggers;

  public BogusAlarmWorkUnit(Map<AlarmType, Runnable> alarmTriggers) {
    this.alarmTriggers = alarmTriggers;
  }

  @Override
  public Optional<ScheduleInfo> apply(Data inputData) {
    AlarmType type = AlarmType.valueOf(inputData.getString(ALARM_TYPE_PARAMETER_KEY));
    Log.i(TAG, "Trigger bogus alarm for " + type);
    Objects.requireNonNull(alarmTriggers.getOrDefault(type, () -> Log.e(TAG, "Unknown type " + type))).run();
    return Optional.empty();
  }
}
