package com.github.frimtec.android.pikettassist.domain;

import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TestAlarmTest {

  @Test
  void getContext() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    TestAlarm testAlarm = new TestAlarm(testAlarmContext, Instant.now(), OnOffState.ON, "message");
    assertThat(testAlarm.getContext()).isSameAs(testAlarmContext);
  }

  @Test
  void getReceivedTime() {
    Instant receivedTime = Instant.now();
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), receivedTime, OnOffState.ON, "message");
    assertThat(testAlarm.getReceivedTime()).isSameAs(receivedTime);
  }

  @Test
  void getAlertState() {
    OnOffState alertState = OnOffState.ON;
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), Instant.now(), alertState, "message");
    assertThat(testAlarm.getAlertState()).isSameAs(alertState);
  }

  @Test
  void getMessage() {
    String message = "message";
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), Instant.now(), OnOffState.ON, message);
    assertThat(testAlarm.getMessage()).isSameAs(message);
  }
}