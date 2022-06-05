package com.github.frimtec.android.pikettassist.service;

import android.content.Intent;

import androidx.work.Data;

import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
interface WorkUnit extends Function<Data, Optional<ScheduleInfo>> {

  @Override
  Optional<ScheduleInfo> apply(Data intent);
}
