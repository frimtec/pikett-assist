package com.github.frimtec.android.pikettassist.service.system;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging;

import static com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging.AC;
import static com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging.NO;
import static com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging.USB;
import static com.github.frimtec.android.pikettassist.domain.BatteryStatus.Charging.WIRELESS;

public class BatteryService {
  private final Context context;

  public BatteryService(Context context) {
    this.context = context;
  }

  public BatteryStatus batteryStatus() {
    Intent batteryStatus = this.context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    return batteryStatus != null ? new BatteryStatus(getLevel(batteryStatus), getCharging(batteryStatus)) : new BatteryStatus(100, NO);
  }

  private static int getLevel(Intent batteryStatus) {
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    return Math.round(level * 100f / (float) scale);
  }

  private static Charging getCharging(Intent batteryStatus) {
    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
    return !isCharging ? NO : chargePlug == BatteryManager.BATTERY_PLUGGED_AC ? AC : chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS ? WIRELESS : USB;
  }

}
