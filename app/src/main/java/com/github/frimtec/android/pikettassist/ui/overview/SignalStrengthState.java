package com.github.frimtec.android.pikettassist.ui.overview;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.domain.OnOffState;
import com.github.frimtec.android.pikettassist.service.BogusAlarmService;
import com.github.frimtec.android.pikettassist.service.system.SignalStrengthService.SignalLevel;
import com.github.frimtec.android.pikettassist.state.ApplicationPreferences;

import static com.github.frimtec.android.pikettassist.service.BogusAlarmService.AlarmType.LOW_SIGNAL;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.GREEN;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.OFF;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.RED;
import static com.github.frimtec.android.pikettassist.ui.overview.State.TrafficLight.YELLOW;

class SignalStrengthState extends State {

  private static final int MENU_CONTEXT_VIEW = 1;
  private static final int MENU_CONTEXT_DEACTIVATE = 2;
  private static final int MENU_CONTEXT_ACTIVATE = 3;
  private static final int MENU_CONTEXT_BOGUS_ALARM = 4;

  private final StateContext stateContext;

  SignalStrengthState(StateContext stateContext) {
    super(
        R.drawable.ic_signal_cellular_connected_no_internet_1_bar_black_24dp,
        stateContext.getNetworkOperatorName() != null ? String.format("%s %s", stateContext.getString(R.string.state_fragment_signal_level), stateContext.getNetworkOperatorName()) : stateContext.getString(R.string.state_fragment_signal_level),
        stateContext.isSuperviseSignalStrength() ? (stateContext.getShiftState().isOn() ? getSignalStrength(stateContext) : stateContext.getString(R.string.general_enabled)) : stateContext.getString(R.string.general_disabled),
        null,
        getSignalStrengthTrafficLight(stateContext)
    );
    this.stateContext = stateContext;
  }

  private static String getSignalStrength(StateContext stateContext) {
    return stateContext.getSignalStrengthLevel().toString(stateContext.getContext());
  }

  private static TrafficLight getSignalStrengthTrafficLight(StateContext stateContext) {
    if (!stateContext.isSuperviseSignalStrength()) {
      return YELLOW;
    } else if (stateContext.getShiftState().getState() == OnOffState.OFF) {
      return OFF;
    } else if (stateContext.getSignalStrengthLevel().ordinal() <= SignalLevel.NONE.ordinal()) {
      return RED;
    } else if (stateContext.getSignalStrengthLevel().ordinal() <= ApplicationPreferences.instance().getSuperviseSignalStrengthMinLevel(stateContext.getContext())) {
      return YELLOW;
    } else {
      return GREEN;
    }
  }

  @Override
  public void onClickAction(Context context) {
    Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(Context context, ContextMenu menu) {
    menu.add(Menu.NONE, MENU_CONTEXT_VIEW, Menu.NONE, R.string.list_item_menu_view);
    if (ApplicationPreferences.instance().getSuperviseSignalStrength(context)) {
      menu.add(Menu.NONE, MENU_CONTEXT_DEACTIVATE, Menu.NONE, R.string.list_item_menu_deactivate);
    } else {
      menu.add(Menu.NONE, MENU_CONTEXT_ACTIVATE, Menu.NONE, R.string.list_item_menu_activate);
    }
    menu.add(Menu.NONE, MENU_CONTEXT_BOGUS_ALARM, Menu.NONE, R.string.list_item_menu_bogus_alarm);
  }

  @Override
  public boolean onContextItemSelected(Context context, MenuItem item) {
    switch (item.getItemId()) {
      case MENU_CONTEXT_VIEW:
        onClickAction(context);
        return true;
      case MENU_CONTEXT_DEACTIVATE:
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, false);
        stateContext.refreshFragment();
        return true;
      case MENU_CONTEXT_ACTIVATE:
        ApplicationPreferences.instance().setSuperviseSignalStrength(context, true);
        stateContext.refreshFragment();
        return true;
      case MENU_CONTEXT_BOGUS_ALARM:
        BogusAlarmService.scheduleBogusAlarm(context, LOW_SIGNAL);
        return true;
      default:
        return false;
    }
  }
}
