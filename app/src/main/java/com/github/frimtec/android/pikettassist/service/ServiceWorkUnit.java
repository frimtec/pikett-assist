package com.github.frimtec.android.pikettassist.service;

import android.content.Intent;

import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
interface ServiceWorkUnit extends Function<Intent, Optional<ScheduleInfo>> {

  @Override
  Optional<ScheduleInfo> apply(Intent intent);
}
