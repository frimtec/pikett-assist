package com.github.frimtec.android.pikettassist.ui.overview;

import android.annotation.SuppressLint;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

class BatteryState extends State {

  @SuppressLint("DefaultLocale")
  BatteryState(StateContext stateContext) {
    super(
        getBatteryIcon(stateContext),
        stateContext.getString(R.string.state_fragment_battery),
        String.format("%d%% %s", stateContext.getBatteryStatus().getLevel(), chargingText(stateContext)),
        null,
        getBatteryLevelTrafficLight(stateContext)
    );
  }

  private static int getBatteryIcon(StateContext stateContext) {
    BatteryStatus batteryStatus = stateContext.getBatteryStatus();
    if (batteryStatus.getCharging().isCharging()) {
      return R.drawable.ic_baseline_battery_charging_full_24;
    } else if (batteryStatus.getLevel() <= ApplicationPreferences.instance().getBatteryWarnLevel(stateContext.getContext())) {
      return R.drawable.ic_battery_alert_black_24dp;
    }
    return R.drawable.ic_baseline_battery_std_24;
  }

  private static TrafficLight getBatteryLevelTrafficLight(StateContext stateContext) {
    if (stateContext.getPikettState() == OnOffState.OFF) {
      return TrafficLight.OFF;
    }
    int level = stateContext.getBatteryStatus().getLevel();
    int warnLevel = ApplicationPreferences.instance().getBatteryWarnLevel(stateContext.getContext());
    if (level <= (warnLevel / 2)) {
      return RED;
    } else if (level <= warnLevel) {
      return YELLOW;
    }
    return GREEN;
  }

  private static String chargingText(StateContext stateContext) {
    BatteryStatus.Charging charging = stateContext.getBatteryStatus().getCharging();
    return charging.isCharging() ? String.format("(%s - %s)", stateContext.getString(R.string.state_fragment_battery_charging), charging.name()) : "";
  }

}
