package com.github.frimtec.android.pikettassist.ui.overview;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmWorker.AlarmType.LOW_SIGNAL;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.service.BogusAlarmWorker;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

class InternetState extends State {

  private static final int MENU_CONTEXT_VIEW = 1;
  private static final int MENU_CONTEXT_DEACTIVATE = 2;
  private static final int MENU_CONTEXT_ACTIVATE = 3;
  private static final int MENU_CONTEXT_BOGUS_ALARM = 4;

  private final StateContext stateContext;

  InternetState(StateContext stateContext) {
    super(
        stateContext.getInternetAvailability().getIconResource(),
        stateContext.getString(R.string.state_fragment_internet),
        stateContext.isSuperviseSignalStrength() ? stateContext.getInternetAvailability().toString(stateContext.getContext()) : stateContext.getString(R.string.general_disabled),
        null,
        getTrafficLight(stateContext)
    );
    this.stateContext = stateContext;
  }

  private static TrafficLight getTrafficLight(StateContext stateContext) {
    if (!stateContext.isSuperviseSignalStrength()) {
      return YELLOW;
    }
    return stateContext.getInternetAvailability().isAvailable() ? GREEN : RED;
  }

  @Override
  public void onClickAction(Context context) {
    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    stateContext.addContextMenu(menu, MENU_CONTEXT_VIEW, R.string.list_item_menu_view);
    if (ApplicationPreferences.instance().getSuperviseSignalStrength(context)) {
      stateContext.addContextMenu(menu, MENU_CONTEXT_DEACTIVATE, R.string.list_item_menu_deactivate);
    } else {
      stateContext.addContextMenu(menu, MENU_CONTEXT_ACTIVATE, R.string.list_item_menu_activate);
    }
    stateContext.addContextMenu(menu, MENU_CONTEXT_BOGUS_ALARM, R.string.list_item_menu_bogus_alarm);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW -> {
        onClickAction(context);
        return true;
      }
      case MENU_CONTEXT_DEACTIVATE -> {
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, false);
        stateContext.refreshFragment();
        return true;
      }
      case MENU_CONTEXT_ACTIVATE -> {
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, true);
        stateContext.refreshFragment();
        return true;
      }
      case MENU_CONTEXT_BOGUS_ALARM -> {
        BogusAlarmWorker.scheduleBogusAlarm(context, LOW_SIGNAL);
        return true;
      }
      default -> {
        return false;
      }
    }
  }
}
