package com.github.frimtec.android.pikettassist.service;

import android.content.Intent;

import com.github.frimtec.android.pikettassist.service.BogusAlarmService.AlarmType;
import com.github.frimtec.android.pikettassist.service.system.AlarmService.ScheduleInfo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmService.INTENT_EXTRA_ALARM_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BogusAlarmServiceWorkUnitTest {

  @ParameterizedTest
  @EnumSource(AlarmType.class)
  void apply(AlarmType alarmType) {
    // arrange
    Intent intent = mock(Intent.class);
    when(intent.getStringExtra(INTENT_EXTRA_ALARM_TYPE)).thenReturn(alarmType.name());
    Map<AlarmType, Runnable> alarmTriggers = alarmTriggers();

    ServiceWorkUnit workUnit = new BogusAlarmServiceWorkUnit(alarmTriggers);

    // act
    Optional<ScheduleInfo> scheduleInfo = workUnit.apply(intent);

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