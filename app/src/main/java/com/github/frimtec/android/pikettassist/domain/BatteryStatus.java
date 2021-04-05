package com.github.frimtec.android.pikettassist.domain;

public final class BatteryStatus {

  public enum Charging {
    NO(false),
    AC(true),
    USB(true),
    WIRELESS(true);

    boolean charging;

    Charging(boolean charging) {
      this.charging = charging;
    }

    public boolean isCharging() {
      return charging;
    }
  }

  private final int level;
  private final Charging charging;

  public BatteryStatus(int level, Charging charging) {
    this.level = level;
    this.charging = charging;
  }

  public int getLevel() {
    return level;
  }

  public Charging getCharging() {
    return charging;
  }
}
