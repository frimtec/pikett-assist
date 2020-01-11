package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.os.PowerManager;

public class PowerService {

  private static final String TAG = "PowerService";

  private final PowerManager powerManager;

  public PowerService(Context context) {
    this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
  }

  public boolean isIgnoringBatteryOptimizations(String packageName) {
    return this.powerManager.isIgnoringBatteryOptimizations(packageName);
  }

  public PowerManager.WakeLock newWakeLock(String name) {
    return this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + ":" + name);
  }
}
