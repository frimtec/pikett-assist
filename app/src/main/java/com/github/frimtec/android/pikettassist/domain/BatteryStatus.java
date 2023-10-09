package com.github.frimtec.android.pikettassist.domain;

public record BatteryStatus(int level, Charging charging) {

  public enum Charging {
    NO(false),
    AC(true),
    USB(true),
    WIRELESS(true);

    final boolean charging;

    Charging(boolean charging) {
      this.charging = charging;
    }

    public boolean isCharging() {
      return charging;
    }
  }

}
