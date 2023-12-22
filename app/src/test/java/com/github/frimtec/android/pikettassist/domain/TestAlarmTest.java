package com.github.frimtec.android.pikettassist.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import java.time.Instant;

class TestAlarmTest {

  @Test
  void getContext() {
    TestAlarmContext testAlarmContext = new TestAlarmContext("context");
    TestAlarm testAlarm = new TestAlarm(testAlarmContext, Instant.now(), OnOffState.ON, "message", null);
    assertThat(testAlarm.context()).isSameAs(testAlarmContext);
  }

  @Test
  void getReceivedTime() {
    Instant receivedTime = Instant.now();
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), receivedTime, OnOffState.ON, "message", null);
    assertThat(testAlarm.receivedTime()).isSameAs(receivedTime);
  }

  @Test
  void getAlertState() {
    OnOffState alertState = OnOffState.ON;
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), Instant.now(), alertState, "message", null);
    assertThat(testAlarm.alertState()).isSameAs(alertState);
  }

  @Test
  void getMessage() {
    String message = "message";
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), Instant.now(), OnOffState.ON, message, null);
    assertThat(testAlarm.message()).isSameAs(message);
  }

  @Test
  void nameForAlias() {
    String alias = "alias";
    TestAlarm testAlarm = new TestAlarm(new TestAlarmContext("context"), Instant.now(), OnOffState.ON, "message", alias);
    assertThat(testAlarm.alias()).isSameAs(alias);
    assertThat(testAlarm.name()).isSameAs(alias);
  }
}