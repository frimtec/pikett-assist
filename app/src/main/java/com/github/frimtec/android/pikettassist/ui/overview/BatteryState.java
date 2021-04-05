package com.github.frimtec.android.pikettassist.ui.overview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.BatteryStatus;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

class BatteryState extends State {

  private static final int MENU_CONTEXT_VIEW = 1;
  private static final int MENU_CONTEXT_DEACTIVATE = 2;
  private static final int MENU_CONTEXT_ACTIVATE = 3;

  private final StateContext stateContext;

  BatteryState(StateContext stateContext) {
    super(
        getBatteryIcon(stateContext),
        stateContext.getString(R.string.state_fragment_battery),
        getText(stateContext),
        null,
        getBatteryLevelTrafficLight(stateContext)
    );
    this.stateContext = stateContext;
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
    if (!ApplicationPreferences.instance().getSuperviseBatteryLevel(stateContext.getContext())) {
      return YELLOW;
    }
    if (stateContext.getPikettState() == OnOffState.OFF) {
      return TrafficLight.OFF;
    }
    int level = stateContext.getBatteryStatus().getLevel();
    int warnLevel = ApplicationPreferences.instance().getBatteryWarnLevel(stateContext.getContext());
    if (level <= warnLevel) {
      return RED;
    }
    return GREEN;
  }

  @SuppressLint("DefaultLocale")
  private static String getText(StateContext stateContext) {
    if (!ApplicationPreferences.instance().getSuperviseBatteryLevel(stateContext.getContext())) {
      return stateContext.getString(R.string.general_disabled);
    }
    BatteryStatus.Charging charging = stateContext.getBatteryStatus().getCharging();
    String chargingText = charging.isCharging() ? String.format("(%s - %s)", stateContext.getString(R.string.state_fragment_battery_charging), charging.name()) : "";
    return String.format("%d%% %s", stateContext.getBatteryStatus().getLevel(), chargingText);
  }

  @Override
  public void onClickAction(Context context) {
    stateContext.getContext().startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY));
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW, Menu.NONE, R.string.list_item_menu_view);
    if (ApplicationPreferences.instance().getSuperviseBatteryLevel(context)) {
      menu.add(Menu.NONE, MENU_CONTEXT_DEACTIVATE, Menu.NONE, R.string.list_item_menu_deactivate);
    } else {
      menu.add(Menu.NONE, MENU_CONTEXT_ACTIVATE, Menu.NONE, R.string.list_item_menu_activate);
    }
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW:
        onClickAction(context);
        return true;
      case MENU_CONTEXT_DEACTIVATE:
        ApplicationPreferences.instance().setSuperviseBatteryLevel(context, false);
        stateContext.refreshFragment();
        return true;
      case MENU_CONTEXT_ACTIVATE:
        ApplicationPreferences.instance().setSuperviseBatteryLevel(context, true);
        stateContext.refreshFragment();
        return true;
      default:
        return false;
    }
  }

}
