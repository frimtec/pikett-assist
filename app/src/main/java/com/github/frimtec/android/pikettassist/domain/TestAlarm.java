package com.github.frimtec.android.pikettassist.domain;

import java.time.Instant;

public class TestAlarm {
  private final TestAlarmContext context;
  private final Instant receivedTime;
  private final OnOffState alertState;
  private final String message;

  public TestAlarm(TestAlarmContext context, Instant receivedTime, OnOffState alertState, String message) {
    this.context = context;
    this.receivedTime = receivedTime;
    this.alertState = alertState;
    this.message = message;
  }

  public TestAlarmContext getContext() {
    return context;
  }

  public Instant getReceivedTime() {
    return receivedTime;
  }

  public OnOffState getAlertState() {
    return alertState;
  }

  public String getMessage() {
    return message;
  }
}
