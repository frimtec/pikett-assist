package com.github.frimtec.android.pikettassist.service;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.ALARM_TYPE_PARAMETER_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import androidx.work.Data;

import com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.AlarmType;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

class BogusAlarmWorkerWorkUnitTest {

  @ParameterizedTest
  @EnumSource(AlarmType.class)
  void apply(AlarmType alarmType) {
    // arrange
    Data inputData = new Data.Builder()
        .putString(ALARM_TYPE_PARAMETER_KEY, alarmType.name())
        .build();
    Map<AlarmType, Runnable> alarmTriggers = alarmTriggers();

    WorkUnit workUnit = new BogusAlarmWorkUnit(alarmTriggers);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(inputData);

    // assert
    assertThat(scheduleInfo).isEqualTo(Optional.empty());
    verify(alarmTriggers.get(alarmType), times(1)).run();
    EnumSet.allOf(AlarmType.class).stream()
        .filter(element -> element != alarmType)
        .forEach(type -> verifyNoInteractions(alarmTriggers.get(type)));
  }

  private Map<AlarmType, Runnable> alarmTriggers() {
    Map<AlarmType, Runnable> alarmTriggers = new EnumMap<>(AlarmType.class);
    alarmTriggers.put(AlarmType.ALERT, mock(Runnable.class));
    alarmTriggers.put(AlarmType.LOW_SIGNAL, mock(Runnable.class));
    alarmTriggers.put(AlarmType.MISSING_TEST_ALARM, mock(Runnable.class));
    return alarmTriggers;
  }
}