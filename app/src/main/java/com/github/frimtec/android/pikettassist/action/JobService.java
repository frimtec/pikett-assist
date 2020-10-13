package com.github.frimtec.android.pikettassist.action;

public enum JobService {
  LOW_SIGNAL_SERVICE(1000),
  PIKETT_SERVICE(1100),
  TEST_ALARM_SERVICE(1200);

  private final int id;

  JobService(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
