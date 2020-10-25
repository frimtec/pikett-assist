package com.github.frimtec.android.pikettassist.service;

import android.content.Intent;
import android.util.Log;

import com.github.frimtec.android.pikettassist.service.BogusAlarmService.AlarmType;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import java.util.Map;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmService.INTENT_EXTRA_ALARM_TYPE;

class BogusAlarmServiceWorkUnit implements ServiceWorkUnit {

  private static final String TAG = "BogusAlarmServiceWorkUnit";

  private final Map<AlarmType, Runnable> alarmTriggers;

  public BogusAlarmServiceWorkUnit(Map<AlarmType, Runnable> alarmTriggers) {
    this.alarmTriggers = alarmTriggers;
  }

  @Override
  public Optional<ScheduleInfo> apply(Intent intent) {
    AlarmType type = AlarmType.valueOf(intent.getStringExtra(INTENT_EXTRA_ALARM_TYPE));
    Log.i(TAG, "Trigger bogus alarm for " + type);
    alarmTriggers.getOrDefault(type, () -> Log.e(TAG, "Unknown type " + type)).run();
    return Optional.empty();
  }
}
